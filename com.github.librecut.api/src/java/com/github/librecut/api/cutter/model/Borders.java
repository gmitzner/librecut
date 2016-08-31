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
package com.github.librecut.api.cutter.model;

public class Borders implements IBorders {

	private final double topBorder;
	private final double bottomBorder;
	private final double leftBorder;
	private final double rightBorder;

	public Borders(double topBorder, double bottomBorder, double leftBorder, double rightBorder) {

		this.topBorder = topBorder;
		this.bottomBorder = bottomBorder;
		this.leftBorder = leftBorder;
		this.rightBorder = rightBorder;
	}

	@Override
	public double getTopBorder() {
		return topBorder;
	}

	@Override
	public double getBottomBorder() {
		return bottomBorder;
	}

	@Override
	public double getLeftBorder() {
		return leftBorder;
	}

	@Override
	public double getRightBorder() {
		return rightBorder;
	}
}
