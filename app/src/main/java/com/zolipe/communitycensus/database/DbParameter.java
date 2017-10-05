package com.zolipe.communitycensus.database;

import java.util.ArrayList;

public class DbParameter {
	private ArrayList<ArrayList<Object>> parameters = null;
	
	public void addParamterList(ArrayList<Object> params) {
		if(parameters == null) {
			parameters = new ArrayList<ArrayList<Object>>();
		}
		parameters.add(params);
	}
	
	public ArrayList<Object> getParameters(int index) {
		ArrayList<Object> result = null;
		if(parameters != null) {
			result = parameters.get(index);
		}
		return result;
	}
	
	public String[] getStringArrayParameters(int index) throws Exception
	{
		String[] result = null;
		if(index >= this.parameters.size()) {
			throw new Exception();
		}
		result = this.parameters.get(index).toArray(new String[0]);
		return result;
	}
	
	public Object[] getObjectArrayParameters(int index) throws Exception
	{
		Object[] result = null;
		if(index > this.parameters.size()) {
			throw new Exception();
		}
		ArrayList<Object> tmpArray = this.parameters.get(index);
		if(tmpArray != null) {
			result = tmpArray.toArray();			
		}
		return result;
	}
	
	public int size()
	{
		int result = 0;
		result = (this.parameters != null) ? this.parameters.size() : 0;
		return result;
	}
}

