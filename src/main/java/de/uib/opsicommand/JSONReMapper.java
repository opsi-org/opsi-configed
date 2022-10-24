package de.uib.opsicommand;

/*  Copyright (c) 2006-2012 uib.de
 
Usage of this portion of software is allowed unter the restrictions of the GPL

*/

import utils.*;
import org.json.*;
import de.uib.utilities.logging.*;
import java.util.*;

/**
*  This class contains utility methods for remapping JSON data
* 
* @author Rupert Roeder 
*/

public class JSONReMapper
{
	private final static String CLASSNAME  = JSONReMapper.class.getName();

	public final static String NullRepresenter = "null";

	public static String getErrorFromResponse(JSONObject retrieved)
	{
		String errorMessage = null;

		try
		{
			if ( ! retrieved.isNull("error")  )
			{

				JSONObject jOError = retrieved.optJSONObject("error");

				if  ( (jOError != null) && ( ! jOError.isNull("class") ) && ( ! jOError.isNull("message") ) )
				{
					errorMessage = " [" + jOError.get("class") + "] " + jOError.get("message");
				}
				else
				{
					errorMessage = " " + retrieved.get("error");
				}
			}
		}
		catch (JSONException jex)
		{
			errorMessage = "JSON Error on retrieving result value,  " + jex.toString();
		}

		return errorMessage;
	}

	public static Map<String, Object> getResponses(JSONObject retrieved)
	{
		Map<String, Object> result = new HashMap<String, Object>();

		Map<String, Object> result0 = getMapResult(retrieved);

		try
		{

			for (String key : result0.keySet())
			{
				JSONObject jO =  (JSONObject) (result0.get(key));
				HashMapX  response = new HashMapX (jO, true);
				//String value = "";


				if (response.get("error") == null)
				{
					java.util.List list = (java.util.List) response.get("result");

					//logging.debug("JSONReMapper getResponses " + list);
					//value = "" + list;

					result.put(key, list);
				}
				else
				{
					//value = (String) response.get("error");
					String str = "" +  response.get("error");
					result.put(key, str);
				}
				//result.put(key, value);
			}
		}

		catch(Exception ex)
		{
			logging.error("JSONReMapper getResponses " + ex);
		}

		logging.debug("JSONReMapper getResponses  result " + result);

		return result;
	}

	
	public static boolean checkForNotValidOpsiMethod (JSONObject retrieved)
	{
		//logging.debug("JSONReMapper: checkForNotValidOpsiMethod " + getErrorFromResponse(retrieved));   
		if (
			retrieved != null && getErrorFromResponse(retrieved) != null 
			&&  getErrorFromResponse(retrieved).indexOf("Opsi rpc error: Method") > -1
			&&  getErrorFromResponse(retrieved).endsWith("is not valid")
		)
		{
			logging.info("JSONReMapper: checkForNotValidOpsiMethod " + getErrorFromResponse(retrieved));   
			return false;
		}
		
			
		return true;
	}


	public static boolean checkResponse (JSONObject retrieved)
	{
		boolean responseFound = true;

		logging.debug(CLASSNAME + ".checkResponse " + logging.LEVEL_DEBUG);
		//"retrieved JSONObject " + retrieved);

		if ( retrieved  == null )
		{
			responseFound = false;
		}
		else
		{
			Object resultValue = null;

			try
			{
				/*
				if ( ! retrieved.isNull("error")  ) 
			{
					String logMessage = "Opsi service error: ";
					JSONObject jOError = retrieved.optJSONObject("error");
					
					if  ( (jOError != null) && ( ! jOError.isNull("class") ) && ( ! jOError.isNull("message") ) )
					{

						logMessage = logMessage + " [" + jOError.get("class") + "] " + jOError.get("message");
					}
					else
					{
						logMessage = logMessage + " " + retrieved.get("error");
					}
					logging.error(logMessage);
					responseFound = false;
			}
				*/

				String errorMessage = getErrorFromResponse(retrieved);

				if (errorMessage != null)
				{
					//responseFound = false;
					String logMessage = "Opsi service error: " + errorMessage;
					logging.error(logMessage);
				}
				else
				{
					resultValue =  retrieved.get("result");
					//System.out.println (" checkResponse resultValue ------------- " + resultValue);
				}
			}
			catch (JSONException jex)
			{
				logging.error("JSON Error on retrieving result value,  " + jex.toString());
			}

			if  ( resultValue  == null )
			{
				logging.debug(CLASSNAME + ": "
				              + " checkResponse " +  logging.LEVEL_DEBUG, "Null result in response ");
				responseFound = false;
			}
		}

		return responseFound;
	}


	static public Map<String, Map<String, Object>> getMap2_Object(Object retrieved)
	{
		HashMap<String, Map<String, Object>> result = new HashMap<String, Map<String, Object>>();
		HashMap<String, Map<String, Object>> resultNull = new HashMap<String, Map<String, Object>>();

		try
		{
			JSONObject jO = (JSONObject) retrieved;
			if (checkResponse(jO))
			{
				JSONObject jOResult = jO.optJSONObject ("result");
				JSONObjectX jOX = new JSONObjectX(jOResult);

				if (!jOX.isMap())
				{
					logging.error(CLASSNAME + "map expected " + jOX);
				}
				else
				{
					logging.debug(CLASSNAME + "map retrieved ");
					//+ jOX.getMap());
					Map map0  = jOX.getMap();

					Iterator iter0 = map0.keySet().iterator();
					while (iter0.hasNext())
					{
						String key1 = (String) iter0.next();

						JSONObjectX jOX1 = new JSONObjectX( (JSONObject) map0.get(key1) );

						if (!jOX1.isMap())
						{
							logging.error(CLASSNAME + "map expected in level 2 " + jOX1);
							result = resultNull;
						}
						else
						{
							result.put( key1, jOX1.getMap() );
						}
					}
				}
			}
		}
		catch (Exception ex)
		{
			logging.error("this, getMap2_Object : " + ex.toString());
		}

		return result;

	}

	static public Map<String, Map<String, Map<String, Object>>> getMap3_Object(Object retrieved)
	{
		HashMap<String, Map<String, Map<String, Object>>> result = new HashMap<String, Map<String, Map<String, Object>>>();
		try
		{
			JSONObject jO = (JSONObject) retrieved;
			if (checkResponse(jO))
			{
				JSONObject jOResult = jO.optJSONObject ("result");

				HashMap<String, JSONObject> map0  = new HashMapX<String, JSONObject>(jOResult);
				//logging.debug(this, " map0: " + map0);

				Iterator iter0 = map0.keySet().iterator();
				while (iter0.hasNext())
				{
					String key1 = (String) iter0.next();  //e.g. client
					HashMap<String, JSONObject> map1 =new HashMapX<String, JSONObject>(  (JSONObject) map0.get(key1) ); //e.g. map of 1 client values
					//logging.debug(this, " key1 " + key1 + " value " + map1);
					HashMap<String, Map<String, Object>>  map1R = new HashMap<String, Map<String, Object>>(); // to produce

					Iterator iter1 = map1.keySet().iterator();
					while (iter1.hasNext())
					{
						String key2 = (String) iter1.next(); //e.g. product
						HashMap<String, Object> map2 = new HashMapX<String, Object>((JSONObject) map1.get(key2), true); //e.g. product values;
						//logging.debug(this, " key2 " + key2 + " value " + map2);
						map1R.put(key2, map2);
						//logging.debug(this, " map1R.get(key2) " + map1R.get(key2));
					}

					result.put(key1, map1R);
				}
			}
		}
		catch (Exception ex)
		{
			logging.debug(CLASSNAME + ".getMap3_String: " + ex.toString());
		}

		return result;

	}



	static public List<Map<String, String>> getListOfStringMaps(Object retrieved)
	{
		List<Map<String, String>> result = new ArrayList<Map<String, String>>();
		List jsonList = null;
		try
		{
			JSONObject jO = (JSONObject) retrieved;
			if  (checkResponse (jO))
			{
				jsonList = getJsonList (jO, "result");
			}
		}
		catch (Exception ex)
		{
			logging.error("JSONReMapper: Exception on getting list for key \"result\" " + ex.toString());
		}

		JSONObject item = null;

		try
		{

			Iterator iter = jsonList.iterator();

			int count = 0;
			while (iter.hasNext())
			{
				count++;
				item = (JSONObject) iter.next();
				Map<String, String> mapItem = (Map<String, String>) JSONReMapper.deriveStandard( item );
				result.add(mapItem);

			}
			assert jsonList.size() == result.size(): " getListOfMaps did not work, jsonList.size " + jsonList.size() + ", remapped " + result.size();
		}
		catch (Exception ex)
		{
			logging.error("JSONReMapper: Exception on reproducing  " + item + ", " + ex);
		}

		return result;
	}


	static public List<Map<String, Object>> getListOfMaps(Object retrieved)
	{
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		List jsonList = null;
		try
		{
			JSONObject jO = (JSONObject) retrieved;
			if  (checkResponse (jO))
			{
				jsonList = getJsonList (jO, "result");
			}
		}
		catch (Exception ex)
		{
			logging.error("JSONReMapper: Exception on getting list for key \"result\" " + ex.toString());
		}

		JSONObject item = null;

		try
		{

			Iterator iter = jsonList.iterator();

			int count = 0;
			while (iter.hasNext())
			{
				count++;
				item = (JSONObject) iter.next();
				Map<String, Object> mapItem = (Map<String, Object>) JSONReMapper.deriveStandard( item );
				result.add(mapItem);

			}
			assert jsonList.size() == result.size(): " getListOfMaps did not work, jsonList.size " + jsonList.size() + ", remapped " + result.size();
		}
		catch (Exception ex)
		{
			logging.error("JSONReMapper: Exception on reproducing  " + item + ", " + ex);
		}

		return result;
	}


	static public List<JSONObject>getListOfJSONObjects(JSONObject jO, String key)
	{
		ArrayList<JSONObject> result = new ArrayList<JSONObject>();
		JSONArray  jA = jO.optJSONArray (key);

		if (jA != null)
		{
			result = jA.toList();
		}

		return result;
	}

	static public List<List<String>>getListOfListsOfStrings(JSONObject jO, String key)
	{
		List<List<String>> list0 = new ArrayList<List<String>>();
		JSONArray  jA = jO.optJSONArray (key);

		int size0 = jA.length();
		try
		{
			for (int i = 0; i < size0; i++)
			{
				JSONArray element0 = jA.getJSONArray(i);
				int size1 = element0.length();

				List<String> list1 = new ArrayList<String>();

				for (int j = 0; j < size1; j++)
				{
					//System.out.println("element1 " + element0.get(j));
					list1.add("" + element0.get(j));
				}

				list0.add(list1);
			}
		}
		catch (JSONException jex)
		{
			logging.error("JSONReMapper: Exception on getting list of lists of JSONObjects " + jex.toString());
		}


		return list0;
	}

	static public List<List<String>>getJsonListOfStringLists(JSONObject jO, String key)
	{
		ArrayList<List<String>> result = new ArrayList<List<String>>();

		List<Object> list1 = getJsonList(jO, key);


		try
		{
			for (Object ob1 : list1)
			{
				JSONArray jA = (JSONArray) ob1;

				ArrayList<String> row = null;

				if (jA != null)
				{
					row = new ArrayList(jA.length());
					for  (int i =0 ;  i < jA.length();  i++)
					{
						//logging.debug(this, "getJSONList, add item ---- " + jA.get(i));

						if (isNull( jA.get(i) ))
							row.add("");

						else
							row.add ( "" +  jA.get(i) );
					}

					result.add(row);
				}
			}
		}
		catch (JSONException jex)
		{
			logging.error("JSONReMapper: Exception on getting list of stringlists " + jex.toString());
		}


		//System.out.println(	" getJsonListOfStringLists  " + result);
		//System.exit(0);

		return result;
	}


	static public List getJsonList(JSONObject jO, String key)
	{
		ArrayList result = new ArrayList();
		try
		{
			JSONArray  jA = jO.optJSONArray (key);

			if (jA != null)
			{
				result = new ArrayList(jA.length());
				for  (int i =0 ;  i < jA.length();  i++)
				{
					//logging.debug(this, "getJSONList, add item ---- " + jA.get(i));
					result.add (  jA.get(i) );
				}
			}

			//logging.debug(this, "getJSONList, JSONArray " + jA);
			//logging.debug(this, "getJSONList, produced size " + result.size());
		}
		catch (JSONException jex)
		{
			logging.error( "JSONReMapper: Exception on getting list " + jex.toString());
		}

		return result;
	}

	static public List<String> getJsonStringList(JSONObject jO, String key)
	{
		ArrayList<String> result = new ArrayList<String>();
		try
		{
			JSONArray  jA = jO.optJSONArray (key);

			//System.out.println ("jA is " + jA.toString());
			if (jA != null)
			{
				result = new ArrayList(jA.length());
				for  (int i =0 ;  i < jA.length();  i++)
				{
					result.add (  "" + jA.get(i) );
				}
			}

			// System.out.println ("result is" + result.toString());
		}
		catch (JSONException jex)
		{
			logging.error("JSONReMapper: Exception on getting list " + jex.toString());
		}

		return result;
	}


	static public List getListResult(JSONObject jO)
	{
		List result = new ArrayList();
		try
		{
			if  (checkResponse (jO))
			{
				result = JSONReMapper.getJsonList (jO, "result");
			}
		}
		catch (Exception ex)
		{
			logging.error(CLASSNAME + "Exception on getting list for key \"result\" " + ex.toString());
		}

		return result;
	}

	static public Map<String, Object> getMap_Object(JSONObject jo)
	//  this method tries to return Java lists in comparison with getMapResult
	{
		Map<String, Object> result = new HashMap<String, Object>();

		try
		{
			JSONObjectX jOX = new JSONObjectX(jo);

			if (!jOX.isMap())
			{
				logging.error("JSONReMapper map expected " + jOX);
			}
			else
				result =  jOX.getMap();

		}
		catch (Exception ex)
		{
			logging.error("JSONReMapper  getMap_Object : " + ex.toString());
		}

		return result;
	}



	static public Map getMapResult (JSONObject jO)
	// yields possibly JSON objects and arrays as values
	// compare getMap_Object
	{
		HashMap result = new HashMap();
		try
		{
			if (checkResponse(jO))
			{
				JSONObject jOResult = jO.optJSONObject ("result") ;
				if  (jOResult != null)
				{
					Iterator iter = jOResult.keys();
					while (iter.hasNext())
					{
						String key = (String) iter.next();
						result.put (key, jOResult.get(key));
					}
				}
			}
		}
		catch (JSONException jex)
		{
			logging.error(CLASSNAME + "Exception on getting Map " + jex.toString());
		}
		return result;
	}


	static public List<String> getStringListResult (JSONObject jO)
	{
		List <String> result = new ArrayList<String>();
		try
		{
			if  (checkResponse (jO))
			{
				result = JSONReMapper.getJsonStringList (jO, "result");
			}

		}
		catch (Exception ex)
		{
			logging.error(CLASSNAME + "Exception on getting list for key \"result\" " + ex.toString());
		}

		return result;
	}

	static public List<List<String>> getListOfStringLists(JSONObject jO)
	{
		List <List<String>> result = new ArrayList<List<String>>();
		try
		{
			if  (checkResponse (jO))
			{
				result = JSONReMapper.getJsonListOfStringLists (jO, "result");
			}

		}
		catch (Exception ex)
		{
			logging.error(CLASSNAME + "Exception on getting list for key \"result\" " + ex.toString());
		}

		return result;
	}



	private static class HashMapX<String, V> extends HashMap<String, V>
	{
		HashMapX(JSONObject jO)
		{
			this(jO, false);
		}

		HashMapX(JSONObject jO, boolean derive)
		{
			super();
			//logging.debug("got JSONObject " + jO);
			try
			{
				if (jO != null)
				{
					Iterator iter = jO.keys();
					while (iter.hasNext())
					{
						Object key =  iter.next();
						//logging.debug("got key " +key);
						if (!jO.isNull((java.lang.String) key))
						{
							if (derive)
								put((String) key, (V) (deriveStandard(jO.get( (java.lang.String) key))));
							else
								put((String) key, (V) (jO.get( (java.lang.String) key)));
						}
						/*
						else
							put((String) key, null);
						*/
					}
				}
			}
			catch(Exception ex)
			{
				logging.error(CLASSNAME + "json transform exception: " + ex);
			}
		}
	}

	static public Object deriveStandard(Object ob)
	{
		if (ob == null)
			return null;

		else if (ob instanceof String)
			return ob;

		else if (ob instanceof JSONArray)
		{
			return ((JSONArray) ob).toList();
			//to do: make recursive
		}

		else if (ob instanceof JSONObject)
		{
			Map<String, Object> map = new HashMap();

			Iterator iter = ((JSONObject) ob).keys();

			while (iter.hasNext())
			{
				String key = null;
				Object value = null;;

				try
				{
					key = (String) iter.next();

					if ( ((JSONObject) ob).isNull(key) )
						map.put(key, null);

					else
					{
						value = ((JSONObject) ob).get(key);
						map.put(key, value);
					}
					//make recursive
				}
				catch (Exception ex)
				{
					logging.error("deriveStandard, key " + key + ", value " + value +", " + ex);
				}
			}
			return map;
		}
		else
			return ob;
	}

	static public boolean isNull(Object ob)
	{
		if (ob == null)
			return true;

		if (ob instanceof String && ((String)ob).equalsIgnoreCase("null"))
			return true;

		if ( (ob instanceof JSONObject) && (  JSONObject.NULL.equals ((JSONObject) ob) ))
			return true;

		return false;
	}
	
	static public boolean equalsNull( String ob)
	{
		return ob == null || ob.equalsIgnoreCase("null");
	}
	
	static public String giveEmptyForNullString(String ob)
	{
		if (ob == null || ob.equalsIgnoreCase("null"))
			return "";
		else
			return ob;
	}


	static public Map<String, String> giveEmptyForNullString(Map<String, String> m)
	{
		for (String key : m.keySet())
		{
			if (isNull(m.get(key)))
				m.put(key, "");
		}

		return m;
	}

	static public Map<String, String> giveEmptyForNull(Map<String, Object> m)
	{
		//logging.info("giveEmptyForNull " + m);
		HashMap<String, String> result = new HashMap<String, String>();
		for (String key : m.keySet())
		{
			if (isNull(m.get(key)))
				result.put(key, "");
			else
				result.put(key, "" +m.get(key));
		}
		//logging.info("giveEmptyForNull "  + result );
		return result;
	}


}
