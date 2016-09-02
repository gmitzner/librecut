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
package com.github.librecut.internal.application;

import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;

public class ApplicationActionBarAdvisor extends ActionBarAdvisor {

	private IWorkbenchAction quitAction;
	private IWorkbenchAction aboutAction;
	private IWorkbenchAction introAction;

	public ApplicationActionBarAdvisor(IActionBarConfigurer configurer) {
		super(configurer);
	}

	@Override
	protected void makeActions(IWorkbenchWindow window) {

		quitAction = ActionFactory.QUIT.create(window);
		register(quitAction);

		aboutAction = ActionFactory.ABOUT.create(window);
		register(aboutAction);

		introAction = ActionFactory.INTRO.create(window);
		register(introAction);
	}

	@Override
	protected void fillMenuBar(IMenuManager menuBar) {

		MenuManager fileMenu = new MenuManager("&File", IWorkbenchActionConstants.M_FILE);
		fileMenu.add(quitAction);
		menuBar.add(fileMenu);

		MenuManager layoutMenu = new MenuManager("&Layout", "layout");
		layoutMenu.add(new GroupMarker("new"));
		layoutMenu.add(new Separator());
		layoutMenu.add(new GroupMarker("open"));
		menuBar.add(layoutMenu);

		MenuManager designMenu = new MenuManager("&Designs", "design");
		designMenu.add(new GroupMarker("new"));
		designMenu.add(new Separator());
		designMenu.add(new GroupMarker("open"));
		menuBar.add(designMenu);

		MenuManager cutterMenu = new MenuManager("&Cutters", "cutter");
		cutterMenu.add(new GroupMarker("actions"));
		menuBar.add(cutterMenu);

		MenuManager helpMenu = new MenuManager("&Help", IWorkbenchActionConstants.M_HELP);
		helpMenu.add(introAction);
		fileMenu.add(new Separator());
		helpMenu.add(aboutAction);
		menuBar.add(helpMenu);
	}
}
