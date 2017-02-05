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

public enum MediaType {

	CardWithoutCraftPaperBacking(100, Messages.MediaType_CardWithoutCraftPaperBacking, 27, 10, 18),
	CardWithCraftPaperBacking(101, Messages.MediaType_CardWithCraftPaperBacking, 27, 10, 18),
	VinylSticker(102, Messages.MediaType_VinylSticker, 10, 10, 18),
	FilmLabel(106, Messages.MediaType_FilmLabel, 14, 10, 18),
	ThickMedia(111, Messages.MediaType_ThickMedia, 27, 10, 18),
	ThinMedia(112, Messages.MediaType_ThinMedia, 2, 10, 18),
	Pen(113, Messages.MediaType_Pen, 10, 10, 0),
	BondPaper13_28lbs(120, Messages.MediaType_BondPaper13_28lbs, 30, 10, 18),
	BristolPaper57_67lbs(121, Messages.MediaType_BristolPaper57_67lbs, 30, 10, 18),
	Cardstock40_60lbs(122, Messages.MediaType_Cardstock40_60lbs, 30, 10, 18),
	Cover40_60lbs(123, Messages.MediaType_Cover40_60lbs, 30, 10, 18),
	FilmDoubleMatteTranslucent(124, Messages.MediaType_FilmDoubleMatteTranslucent, 1, 10, 18),
	FilmVinylWithAdhesiveBack(125, Messages.MediaType_FilmVinylWithAdhesiveBack, 1, 10, 18),
	FilmWindowWithKlingAdhesive(126, Messages.MediaType_FilmWindowWithKlingAdhesive, 1, 10, 18),
	Index90lbs(127, Messages.MediaType_Index90lbs, 30, 10, 18),
	InkjetPhotoPaper28_44lbs(128, Messages.MediaType_InkjetPhotoPaper28_44lbs, 20, 10, 18),
	InkjetPhotoPaper45_75lbs(129, Messages.MediaType_InkjetPhotoPaper45_75lbs, 27, 10, 18),
	MagneticSheet(130, Messages.MediaType_MagneticSheet, 30, 3, 18),
	Offset24_60lbs(131, Messages.MediaType_Offset24_60lbs, 30, 10, 18),
	PrintPaperLightWeight(132, Messages.MediaType_PrintPaperLightWeight, 5, 10, 18),
	PrintPaperMediumWeight(133, Messages.MediaType_PrintPaperMediumWeight, 25, 10, 18),
	StickerSheet(134, Messages.MediaType_StickerSheet, 20, 10, 18),
	Tag100lbs(135, Messages.MediaType_Tag100lbs, 20, 10, 18),
	TextPaper24_70lbs(136, Messages.MediaType_TextPaper24_70lbs, 30, 10, 18),
	VellumBristol57_67lbs(137, Messages.MediaType_VellumBristol57_67lbs, 30, 10, 18),
	WritingPaper24_70lbs(138, Messages.MediaType_WritingPaper24_70lbs, 30, 10, 18);

	private final int mediaValue;
	private final String label;
	private final int defaultPressure;
	private final int defaultSpeed;
	private final int cuttingOffset;

	private MediaType(int mediaValue, String label, int defaultPressure, int defaultSpeed, int cuttingOffset) {

		this.mediaValue = mediaValue;
		this.label = label;
		this.defaultPressure = defaultPressure;
		this.defaultSpeed = defaultSpeed;
		this.cuttingOffset = cuttingOffset;
	}

	public int getMediaValue() {
		return mediaValue;
	}

	public String getLabel() {
		return label;
	}

	public int getDefaultPressure() {
		return defaultPressure;
	}

	public int getDefaultSpeed() {
		return defaultSpeed;
	}

	public int getCuttingOffset() {
		return cuttingOffset;
	}
}
