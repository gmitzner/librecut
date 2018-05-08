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
package com.github.librecut.internal.layouteditor;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

import com.github.librecut.api.cutter.model.IBorders;
import com.github.librecut.api.cutter.model.ICutterDescriptor;
import com.github.librecut.api.cutter.model.LoadingDirection;
import com.github.librecut.api.design.model.IDesign;
import com.github.librecut.api.design.model.IPoint;
import com.github.librecut.api.design.spi.IDesignConsumer;
import com.github.librecut.api.gui.spi.IMediaRenderer;
import com.github.librecut.api.media.model.IMedia;
import com.github.librecut.api.media.model.IMediaSize;
import com.github.librecut.common.cutter.model.Borders;
import com.github.librecut.common.cutter.model.MediaSize;
import com.github.librecut.common.design.model.Point;
import com.github.librecut.internal.application.Activator;
import com.github.librecut.internal.cutter.CutterCore;
import com.github.librecut.internal.media.MediaRendererFactory;
import com.github.librecut.resource.model.DesignEntity;
import com.github.librecut.resource.model.IDesignEntity;
import com.github.librecut.resource.model.ILayout;

public class LayoutEditor extends EditorPart implements IDesignEntityChangeListener, IDesignConsumer {

	public static final String ID = "com.github.librecut.layouteditor";

	private MediaRendererFactory mediaRendererFactory;
	private LayoutCanvas layoutCanvas;

	private String unitName;
	private BigDecimal unitFactor;
	private int precision;

	volatile BigDecimal mediaWidthInches;
	volatile BigDecimal mediaHeightInches;

	volatile BigDecimal topBorderInches;
	volatile BigDecimal leftBorderInches;
	volatile BigDecimal rightBorderInches;
	volatile BigDecimal bottomBorderInches;

	volatile boolean mirrorDesigns;

	private LayoutModel layout;
	private IMediaRenderer mediaRenderer;

	private int nextEntityId;

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {

		ILayout adaptedInput = input.getAdapter(ILayout.class);
		if (adaptedInput == null) {
			throw new PartInitException("Invalid editor input");
		}

		setSite(site);
		setInput(input);
		setPartName(input.getName());

		mediaRendererFactory = new MediaRendererFactory();

		initializeUnit();

		layout = new LayoutModel(adaptedInput);
		mediaRenderer = createMediaRenderer(layout.getMedia());

		IMedia mediaFormat = layout.getMedia();
		IMediaSize mediaSize = mediaFormat.getMediaSize();
		mediaWidthInches = asBigDecimal(mediaSize.getWidth());
		mediaHeightInches = asBigDecimal(mediaSize.getHeight());

		IBorders borders = mediaFormat.getBorders();
		topBorderInches = asBigDecimal(borders.getTopBorder());
		leftBorderInches = asBigDecimal(borders.getLeftBorder());
		rightBorderInches = asBigDecimal(borders.getRightBorder());
		bottomBorderInches = asBigDecimal(borders.getBottomBorder());
	}

	private void initializeUnit() {

		unitName = "mm";
		unitFactor = getFactor(unitName);
		precision = 1;
	}

	private IMediaRenderer createMediaRenderer(IMedia media) throws PartInitException {

		IMediaRenderer mediaRenderer = mediaRendererFactory.create(media);
		if (mediaRenderer == null) {
			IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID,
					"No suitable media renderer implementation found.");
			throw new PartInitException(status);
		}
		return mediaRenderer;
	}

	@Override
	public void createPartControl(Composite parent) {

		Composite baseComposite = new Composite(parent, SWT.NONE);
		baseComposite.setVisible(false);
		baseComposite.setLayout(new GridLayout(2, false));

		Composite controlsComposite = new Composite(baseComposite, SWT.NONE);
		controlsComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1));
		controlsComposite.setLayout(new GridLayout(2, false));

		layoutCanvas = new LayoutCanvas(baseComposite, SWT.NONE, this, () -> layout, mediaRenderer);
		layoutCanvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		createControls(controlsComposite, layoutCanvas);

		layoutCanvas.setDefaultLoadingDirection(LoadingDirection.Top);
		layoutCanvas.redraw();

		baseComposite.setVisible(true);
	}

	private void createControls(Composite parent, final LayoutCanvas layoutCanvas) {

		boolean enableMediaControls = mediaRendererFactory.isDefaultRenderer(mediaRenderer);

		Label label = new Label(parent, SWT.LEFT);
		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		label.setText("Media size");

		Combo mediaSizeCombo = new Combo(parent, SWT.DROP_DOWN);
		mediaSizeCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
		mediaSizeCombo.add("Custom");
		mediaSizeCombo.select(0);
		mediaSizeCombo.setEnabled(enableMediaControls);

		createInputValue(parent, "Width:", mediaWidthInches, value -> {
			mediaWidthInches = value;
			IMedia media = createMedia();
			layout.setMedia(media);
			layoutCanvas.redraw();
		}, enableMediaControls);

		createInputValue(parent, "Height:", mediaHeightInches, value -> {
			mediaHeightInches = value;
			IMedia media = createMedia();
			layout.setMedia(media);
			layoutCanvas.redraw();
		}, enableMediaControls);

		label = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));

		label = new Label(parent, SWT.LEFT);
		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		label.setText("Borders");

		Combo bordersCombo = new Combo(parent, SWT.DROP_DOWN);
		bordersCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
		bordersCombo.add("Custom");
		Collection<ICutterDescriptor> supportedCutters = CutterCore.getSupportedCutters();
		for (ICutterDescriptor descriptor : supportedCutters) {
			bordersCombo.add(descriptor.getName());
		}
		bordersCombo.select(0);
		bordersCombo.setEnabled(enableMediaControls);

		createInputValue(parent, "Top:", topBorderInches, value -> {
			topBorderInches = value;
			IMedia media = createMedia();
			layout.setMedia(media);
			layoutCanvas.redraw();
		}, enableMediaControls);

		createInputValue(parent, "Left:", leftBorderInches, value -> {
			leftBorderInches = value;
			IMedia media = createMedia();
			layout.setMedia(media);
			layoutCanvas.redraw();
		}, enableMediaControls);

		createInputValue(parent, "Right:", rightBorderInches, value -> {
			rightBorderInches = value;
			IMedia media = createMedia();
			layout.setMedia(media);
			layoutCanvas.redraw();
		}, enableMediaControls);

		createInputValue(parent, "Bottom:", bottomBorderInches, value -> {
			bottomBorderInches = value;
			IMedia media = createMedia();
			layout.setMedia(media);
			layoutCanvas.redraw();
		}, enableMediaControls);

		label = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));

		final Button mirrorDesignsButton = new Button(parent, SWT.CHECK);
		mirrorDesignsButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
		mirrorDesignsButton.setText("Mirror designs");
		mirrorDesignsButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				mirrorDesigns = mirrorDesignsButton.getSelection();
				layout.setMirrored(mirrorDesigns);
				layoutCanvas.redraw();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// nothing to do
			}
		});
		mirrorDesignsButton.setSelection(mirrorDesigns);
	}

	private IMedia createMedia() {

		return new IMedia() {

			@Override
			public IMediaSize getMediaSize() {
				return new MediaSize("Custom", mediaWidthInches.doubleValue(), mediaHeightInches.doubleValue());
			}

			@Override
			public IBorders getBorders() {
				return new Borders(topBorderInches.doubleValue(), bottomBorderInches.doubleValue(),
						leftBorderInches.doubleValue(), rightBorderInches.doubleValue());
			}
		};
	}

	private void createInputValue(Composite parent, String labelText, BigDecimal initialValue,
			final Consumer<BigDecimal> consumer, boolean enabled) {

		Label label = new Label(parent, SWT.LEFT);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label.setText(labelText);

		final Text text = new Text(parent, SWT.LEFT | SWT.SINGLE);
		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		text.addFocusListener(new FocusListener() {

			@Override
			public void focusGained(FocusEvent e) {
				// nothing to do
			}

			@Override
			public void focusLost(FocusEvent event) {

				String value = text.getText().trim();
				BigDecimal inches = asInches(value);
				if (inches != null) {
					consumer.accept(inches);
					text.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND));
					text.setText(asValueWithUnit(inches));
				} else {
					text.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_RED));
				}
			}
		});

		text.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				// nothing to do
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

				String value = text.getText().trim();
				BigDecimal inches = asInches(value);
				if (inches != null) {
					consumer.accept(inches);
					text.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND));
					text.setText(asValueWithUnit(inches));
				} else {
					text.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
				}
			}
		});

		text.setText(asValueWithUnit(initialValue));
		text.setEnabled(enabled);
	}

	@Override
	public void consume(IDesign design) {

		String designEntityId = getNextDesignEntityId();
		DesignEntity designEntity = new DesignEntity(designEntityId, design);
		designEntity.setEnabled(true);
		designEntity
				.setPosition(new Point(mediaWidthInches.doubleValue() / 2.0d, mediaHeightInches.doubleValue() / 2.0d));

		List<IDesignEntity> designEntityList = layout.getDesignEntityList();
		designEntityList.add(designEntity);
		layout.notifyDesignEntitiesChanged();
		layoutCanvas.redraw();
	}

	private String getNextDesignEntityId() {

		String id = null;
		while (id == null) {
			id = Integer.toString(nextEntityId++);
			Iterator<IDesignEntity> iterator = layout.getDesignEntityList().iterator();
			while (iterator.hasNext() && (id != null)) {
				if (iterator.next().getId().equals(id)) {
					id = null;
				}
			}
		}
		return id;
	}

	@Override
	public void changePosition(IDesignEntity entity, IPoint position) {

		entity.setPosition(position);
		layout.notifyDesignEntitiesChanged();
		layoutCanvas.redraw();
	}

	@Override
	public void changeRotationAngle(IDesignEntity entity, double angle) {

		entity.setRotationAngle(angle);
		layout.notifyDesignEntitiesChanged();
		layoutCanvas.redraw();
	}

	@Override
	public void changeScale(IDesignEntity entity, double scale) {

		entity.setScale(scale);
		layout.notifyDesignEntitiesChanged();
		layoutCanvas.redraw();
	}

	@Override
	public void removeEntity(IDesignEntity designEntity) {

		List<IDesignEntity> designEntityList = layout.getDesignEntityList();
		designEntityList.remove(designEntity);
		layout.notifyDesignEntitiesChanged();
		layoutCanvas.redraw();
	}

	@Override
	public void setFocus() {
		// nothing to do
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void doSaveAs() {
		// not supported
	}

	private static BigDecimal asBigDecimal(double value) {
		return new BigDecimal(value).round(new MathContext(3, RoundingMode.HALF_UP));
	}

	private String asValueWithUnit(BigDecimal value) {
		return value.divide(unitFactor, precision, RoundingMode.HALF_UP).toPlainString() + " " + unitName;
	}

	private BigDecimal asInches(String value) {

		value = value.trim();
		int i = value.lastIndexOf(' ');
		BigDecimal factor;
		if (i > 0) {
			String unitName = value.substring(i + 1);
			factor = getFactor(unitName);
			if (factor != null) {
				value = value.substring(0, i).trim();
			} else {
				factor = getFactor(this.unitName);
			}
		} else {
			factor = getFactor(this.unitName);
		}

		value = value.replace(" ", "");
		try {
			BigDecimal result = new BigDecimal(value);
			return result.multiply(factor);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private BigDecimal getFactor(String unitName) {

		if ("in".equals(unitName)) {
			return BigDecimal.ONE;
		}
		if ("mm".equals(unitName)) {
			return new BigDecimal(0.0393700787d);
		}
		if ("cm".equals(unitName)) {
			return new BigDecimal(0.393700787d);
		}
		if ("dm".equals(unitName)) {
			return new BigDecimal(3.93700787d);
		}
		if ("m".equals(unitName)) {
			return new BigDecimal(39.3700787d);
		}
		return null;
	}
}
