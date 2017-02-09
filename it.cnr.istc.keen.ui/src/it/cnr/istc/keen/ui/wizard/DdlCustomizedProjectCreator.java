package it.cnr.istc.keen.ui.wizard;

import java.util.List;

import com.google.common.collect.ImmutableList;

public class DdlCustomizedProjectCreator extends DdlProjectCreator {
	
	@Override
	protected List<String> getAllFolders() {
		return ImmutableList.of(getModelFolderName());
	}


}
