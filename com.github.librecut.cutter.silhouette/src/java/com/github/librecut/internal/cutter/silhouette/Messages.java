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
package com.github.librecut.internal.cutter.silhouette;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "com.github.librecut.internal.cutter.silhouette.messages"; //$NON-NLS-1$

	public static String CameoCutterDescriptor_GraphtecSilhouetteCameoName;
	public static String CameoCutterDescriptor_Media;
	public static String CameoCutterDescriptor_Pressure;
	public static String CameoCutterDescriptor_Speed;

	public static String CutterProvider_CutterSearchJobDescription;

	public static String CutterProvider_UsbCutterNamePattern;

	public static String MediaType_BondPaper13_28lbs;

	public static String MediaType_BristolPaper57_67lbs;

	public static String MediaType_Cardstock40_60lbs;

	public static String MediaType_CardWithCraftPaperBacking;

	public static String MediaType_CardWithoutCraftPaperBacking;

	public static String MediaType_Cover40_60lbs;

	public static String MediaType_FilmDoubleMatteTranslucent;

	public static String MediaType_FilmLabel;

	public static String MediaType_FilmVinylWithAdhesiveBack;

	public static String MediaType_FilmWindowWithKlingAdhesive;

	public static String MediaType_Index90lbs;

	public static String MediaType_InkjetPhotoPaper28_44lbs;

	public static String MediaType_InkjetPhotoPaper45_75lbs;

	public static String MediaType_MagneticSheet;

	public static String MediaType_Offset24_60lbs;

	public static String MediaType_Pen;

	public static String MediaType_PrintPaperLightWeight;

	public static String MediaType_PrintPaperMediumWeight;

	public static String MediaType_StickerSheet;

	public static String MediaType_Tag100lbs;

	public static String MediaType_TextPaper24_70lbs;

	public static String MediaType_ThickMedia;

	public static String MediaType_ThinMedia;

	public static String MediaType_VellumBristol57_67lbs;

	public static String MediaType_VinylSticker;

	public static String MediaType_WritingPaper24_70lbs;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
