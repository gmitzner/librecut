/**
 * Copyright (C) 2016 Gerhard Mitzner.
 * 
 * This file is part of LibreCut.
 * 
 * LibreCut is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * LibreCut is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with LibreCut. If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.librecut.internal.cutter.wizards;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import com.github.librecut.api.cutter.model.ICutter;
import com.github.librecut.api.cutter.model.ICutterDescriptor;
import com.github.librecut.api.cutter.model.IParameterDescriptor;
import com.github.librecut.internal.application.Activator;

public class CuttingParameterWizardPage extends WizardPage {

	private static final String PREFERENCE_CUTTERS = "cutters";
	private static final String PREFERENCE_PRESETS = "presets";

	private final ICutter[] selectedCutters;
	private final Map<String, Object> valueMap;
	private final Map<String, List<Control>> controlsMap;

	private ICutter currentCutter;

	private Composite container;
	private Combo presetCombo;
	private Button storeButton;

	public CuttingParameterWizardPage(ICutter[] selectedCutters) {

		super("CuttingParameters", "Cutting parameters", null);

		this.selectedCutters = selectedCutters;
		this.valueMap = new HashMap<String, Object>();
		this.controlsMap = new HashMap<String, List<Control>>();

		setDescription("Please set the cutting parameters according to the media.");
	}

	@Override
	public void createControl(Composite parent) {

		container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		container.setLayout(layout);

		Label label = new Label(container, SWT.RIGHT);
		label.setText("Preset:");
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL, GridData.FILL_VERTICAL, false, false, 1, 1));

		presetCombo = new Combo(container, SWT.NONE);
		presetCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL, GridData.FILL_VERTICAL, true, false, 1, 1));

		storeButton = new Button(container, SWT.PUSH);
		storeButton.setText("Save");
		storeButton.setLayoutData(new GridData(GridData.END, GridData.FILL_VERTICAL, false, false, 1, 1));
		storeButton.setEnabled(false);
		storeButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				String presetName = presetCombo.getText();
				if (!presetName.isEmpty() && areParametersValid()) {
					if (savePreset(presetName)) {
						presetCombo.add(presetName);
						presetCombo.select(presetCombo.getItemCount() - 1);
					}
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// nothing to do
			}
		});

		final Button deleteButton = new Button(container, SWT.PUSH);
		deleteButton.setText("Delete");
		deleteButton.setLayoutData(new GridData(GridData.BEGINNING, GridData.FILL_VERTICAL, false, false, 1, 1));
		deleteButton.setEnabled(false);
		deleteButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				String presetName = presetCombo.getText();
				if (!presetName.isEmpty()) {
					if (deletePreset(presetName)) {
						presetCombo.setText("");
						presetCombo.remove(presetName);
					}
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// nothing to do
			}
		});

		presetCombo.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {

				storeButton.setEnabled(!presetCombo.getText().isEmpty() && areParametersValid());

				String text = presetCombo.getText();
				boolean enableDelete = false;
				for (String item : presetCombo.getItems()) {
					if (item.equals(text)) {
						enableDelete = true;
					}
				}
				deleteButton.setEnabled(enableDelete);
			}
		});
		presetCombo.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				enforcePreset(presetCombo.getText());
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				enforcePreset(presetCombo.getText());
			}
		});

		setControl(container);

		setPageComplete(false);
	}

	@Override
	public void setVisible(boolean visible) {

		if (visible && (currentCutter != selectedCutters[0])) {
			currentCutter = selectedCutters[0];
			updateControls(currentCutter.getDescriptor());
			container.layout(true);
		}
		super.setVisible(visible);
	}

	private void updateControls(ICutterDescriptor descriptor) {

		disposeControls();
		valueMap.clear();
		presetCombo.removeAll();
		loadPresets();

		Collection<IParameterDescriptor<?>> parameters = descriptor.getCuttingParameters();
		for (IParameterDescriptor<?> parameter : parameters) {

			Label label = new Label(container, SWT.RIGHT);
			String labelText = parameter.getLabel(parameter.getName(), Locale.getDefault());
			if ((labelText == null) || labelText.isEmpty()) {
				labelText = parameter.getName();
			}
			label.setText(labelText);
			label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL, GridData.FILL_VERTICAL, false, false, 1, 1));

			List<Control> controlList = new ArrayList<Control>();
			controlList.add(label);

			if (parameter.getValues() != null) {
				createComboControl(parameter, controlList);
			} else if ((parameter.getMinValue() != null) && Integer.class.isAssignableFrom(parameter.getType())) {
				createNumberRangeControl(parameter, controlList);
			} else {
				throw new UnsupportedOperationException();
			}

			controlsMap.put(parameter.getName(), controlList);
		}
	}

	private void loadPresets() {

		List<String> presetNameList = new ArrayList<String>();
		Preferences preferences = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID).node(PREFERENCE_CUTTERS)
				.node(currentCutter.getDescriptor().getName());
		try {
			Collection<IParameterDescriptor<?>> parameters = currentCutter.getDescriptor().getCuttingParameters();
			for (String presetName : preferences.node(PREFERENCE_PRESETS).childrenNames()) {
				Preferences preset = preferences.node(PREFERENCE_PRESETS).node(presetName);
				Set<String> parameterNameSet = new HashSet<String>(Arrays.asList(preset.keys()));
				int parameterCount = 0;
				for (IParameterDescriptor<?> parameter : parameters) {
					if (parameterNameSet.contains(parameter.getName())) {
						parameterCount++;
					}
				}
				if (parameterCount == parameters.size()) {
					presetNameList.add(presetName);
				}
			}
		} catch (BackingStoreException e) {
			return;
		}

		Collections.sort(presetNameList);
		for (String presetName : presetNameList) {
			presetCombo.add(presetName);
		}
	}

	private void enforcePreset(String presetName) {

		Preferences preferences = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID).node(PREFERENCE_CUTTERS)
				.node(currentCutter.getDescriptor().getName());
		Collection<IParameterDescriptor<?>> parameters = currentCutter.getDescriptor().getCuttingParameters();
		Preferences preset = preferences.node(PREFERENCE_PRESETS).node(presetName);
		for (IParameterDescriptor<?> parameter : parameters) {
			if (parameter.getValues() != null) {
				String value = preset.get(parameter.getName(), null);
				if ((value != null) && parameter.isValid(value)) {
					List<Control> controlList = controlsMap.get(parameter.getName());
					Combo combo = (Combo) controlList.get(1);
					String text = parameter.getLabel(value, Locale.getDefault());
					combo.setText(text);
					setComboValue(parameter, text);
				}
			} else if ((parameter.getMinValue() != null) && Integer.class.isAssignableFrom(parameter.getType())) {
				int value = preset.getInt(parameter.getName(), ((Integer) parameter.getMinValue()) - 1);
				if (parameter.isValid(value)) {
					List<Control> controlList = controlsMap.get(parameter.getName());
					Spinner spinner = (Spinner) controlList.get(1);
					spinner.setSelection(value);
					setNumberRangeValue(parameter, value);
				}
			} else {
				throw new UnsupportedOperationException();
			}
		}
	}

	private boolean savePreset(String presetName) {

		Collection<IParameterDescriptor<?>> parameters = currentCutter.getDescriptor().getCuttingParameters();
		for (IParameterDescriptor<?> parameter : parameters) {
			String parameterName = parameter.getName();
			Object value = valueMap.get(parameterName);
			if ((value == null) || !parameter.isValid(value)) {
				return false;
			}
		}

		Preferences preferences = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID).node(PREFERENCE_CUTTERS)
				.node(currentCutter.getDescriptor().getName());
		Preferences preset = preferences.node(PREFERENCE_PRESETS).node(presetName);
		for (IParameterDescriptor<?> parameter : parameters) {
			String parameterName = parameter.getName();
			Object value = valueMap.get(parameterName);
			if (value instanceof Integer) {
				preset.putInt(parameterName, (Integer) value);
			} else if (value instanceof String) {
				preset.put(parameterName, (String) value);
			}
		}
		try {
			preferences.flush();
			return true;
		} catch (BackingStoreException e) {
			return false;
		}
	}

	private boolean deletePreset(String presetName) {

		Preferences preferences = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID).node(PREFERENCE_CUTTERS)
				.node(currentCutter.getDescriptor().getName());
		try {
			if (!preferences.node(PREFERENCE_PRESETS).nodeExists(presetName)) {
				return false;
			}
			preferences.node(PREFERENCE_PRESETS).node(presetName).removeNode();
			preferences.flush();
			return true;
		} catch (BackingStoreException e) {
			return false;
		}
	}

	private void createComboControl(final IParameterDescriptor<?> parameter, List<Control> controlList) {

		final Combo combo = new Combo(container, SWT.NONE);

		Locale locale = Locale.getDefault();
		List<String> items = new ArrayList<String>(parameter.getValues().size());
		for (Object value : parameter.getValues()) {
			String item = parameter.getLabel(value, locale);
			items.add(item);
		}
		Collections.sort(items);
		for (String item : items) {
			combo.add(item);
		}

		combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL, GridData.FILL_VERTICAL, true, false, 1, 1));
		controlList.add(combo);

		combo.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {

				setComboValue(parameter, combo.getText());

				storeButton.setEnabled(!presetCombo.getText().isEmpty() && areParametersValid());
			}
		});

		setComboValue(parameter, combo.getText());
	}

	private void setComboValue(IParameterDescriptor<?> parameter, String text) {

		Locale locale = Locale.getDefault();
		for (Object value : parameter.getValues()) {
			if (text.equals(parameter.getLabel(value, locale))) {
				valueMap.put(parameter.getName(), value);
				setPageComplete(areParametersValid());
				return;
			}
		}
		valueMap.remove(parameter.getName());
		setPageComplete(areParametersValid());
	}

	private void createNumberRangeControl(final IParameterDescriptor<?> parameter, List<Control> controlList) {

		final Spinner spinner = new Spinner(container, SWT.NONE);
		spinner.setMinimum((Integer) parameter.getMinValue());
		spinner.setMaximum((Integer) parameter.getMaxValue());
		spinner.setLayoutData(new GridData(GridData.FILL_HORIZONTAL, GridData.FILL_VERTICAL, false, false, 1, 1));
		controlList.add(spinner);

		spinner.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {

				setNumberRangeValue(parameter, spinner.getSelection());

				storeButton.setEnabled(!presetCombo.getText().isEmpty() && areParametersValid());
			}
		});

		setNumberRangeValue(parameter, spinner.getSelection());
	}

	private void setNumberRangeValue(IParameterDescriptor<?> parameter, int value) {

		valueMap.put(parameter.getName(), value);
		setPageComplete(areParametersValid());
	}

	private boolean areParametersValid() {

		if (currentCutter == null) {
			return false;
		}

		Collection<IParameterDescriptor<?>> parameters = currentCutter.getDescriptor().getCuttingParameters();
		for (IParameterDescriptor<?> parameter : parameters) {
			Object value = valueMap.get(parameter.getName());
			if ((value == null) || !parameter.isValid(value)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void dispose() {

		disposeControls();
		super.dispose();
	}

	private void disposeControls() {

		for (List<Control> controlList : controlsMap.values()) {
			for (Control control : controlList) {
				control.dispose();
			}
		}
		controlsMap.clear();
	}

	public Map<String, Object> getParameterMap() {
		return Collections.unmodifiableMap(valueMap);
	}
}
