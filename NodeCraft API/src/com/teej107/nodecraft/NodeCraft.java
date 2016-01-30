package com.teej107.nodecraft;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.*;
import java.util.*;

import static java.net.URLEncoder.encode;

/**
 * @author teej107
 * @since Jan 30, 2016
 */
public class NodeCraft
{
	private static final String GET = "GET";
	private static final String POST = "POST";

	private static final String URL = "https://api.nodecraft.com/";
	private static URL RATE_LIMIT;

	private int version;
	private JSONParser parser;
	private Map<String, URL> urlMap;

	/**
	 * Connect to NodeCraft with the given username and API key
	 * @param username E-mail or username
	 * @param apiKey API key
	 */
	public NodeCraft(String username, char[] apiKey)
	{
		Authenticator.setDefault(new NodeCraftAuthenticator(username, apiKey));
		this.version = 1;
		this.parser = new JSONParser();
		this.urlMap = new HashMap<>();
	}

	private URL getURL(String s) throws MalformedURLException
	{
		URL url = urlMap.get(s);
		if (url == null)
		{
			url = new URL(s);
			urlMap.put(s, url);
		}
		return url;
	}

	private Map request(String type, String s, Map.Entry... entries) throws IOException
	{
		URL url = getURL(s);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod(type);
		if (entries != null)
		{
			connection.setDoOutput(true);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(), "UTF-8"));
			StringBuilder sb = new StringBuilder();
			for (Map.Entry e : entries)
			{
				if (sb.length() > 0)
				{
					sb.append("&");
				}
				sb.append(encode(e.getKey().toString(), "UTF-8")).append('=').append(encode(e.getValue().toString(), "UTF-8"));
			}
			bw.write(sb.toString());
			bw.flush();
			bw.close();
		}
		try
		{
			return (Map) parser.parse(new InputStreamReader(connection.getInputStream()));
		}
		catch (ParseException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	private String address(String id, String string)
	{
		if (id == null)
			return URL + "v" + version + "/services";
		if (string == null)
			return URL + "v" + version + "/service/" + id;

		return address(id, null) + "/" + string;
	}

	private String coOpVault()
	{
		return URL + "v" + version + "/co-op-vault";
	}

	/**
	 * Get the current rate limiting status
	 * @return Map
	 * @throws IOException
	 */
	public static Map currentRateLimit() throws IOException
	{
		if (RATE_LIMIT == null)
		{
			RATE_LIMIT = new URL(URL + "limits");
		}
		URLConnection connection = RATE_LIMIT.openConnection();
		try
		{
			return (JSONObject) new JSONParser().parse(new InputStreamReader(connection.getInputStream()));
		}
		catch (ParseException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Get a list of all user's services
	 * @return Map
	 * @throws IOException
	 */
	public Map listServices() throws IOException
	{
		return request(GET, address(null, null));
	}

	/**
	 * Get information about a service by given ID
	 * @param id Service ID
	 * @return Map
	 * @throws IOException
	 */
	public Map getService(String id) throws IOException
	{
		return request(GET, address(id, null));
	}

	/**
	 * Get status, disk, ram and CPU stats of a service by given ID
	 * @param id Service ID
	 * @return Map
	 * @throws IOException
	 */
	public Map serviceStatus(String id) throws IOException
	{
		return request(GET, address(id, "stats"));
	}

	/**
	 * Get last 300 lines of console logs of a service by given ID
	 * @param id Service ID
	 * @return Map
	 * @throws IOException
	 */
	public Map consoleLogs(String id) throws IOException
	{
		return request(POST, address(id, "logs"));
	}

	/**
	 * Get input history of a service by given ID
	 * @param id Service ID
	 * @return Map
	 * @throws IOException
	 */
	public Map consoleInputHistory(String id) throws IOException
	{
		return request(POST, address(id, "history"));
	}

	/**
	 * Starts a service by given ID
	 * @param id Service ID
	 * @return Map
	 * @throws IOException
	 */
	public Map startServerProcess(String id) throws IOException
	{
		return request(POST, address(id, "start"));
	}

	/**
	 * Stops a service by given ID
	 * @param id Service ID
	 * @return Map
	 * @throws IOException
	 */
	public Map stopServerProcess(String id) throws IOException
	{
		return request(POST, address(id, "stop"));
	}

	/**
	 * Kills a service by given ID. You should only use this if a "stop" fails. Killing the server forcefully may result in data loss
	 * @param id Service ID
	 * @return Map
	 * @throws IOException
	 */
	public Map killServerProcess(String id) throws IOException
	{
		return request(POST, address(id, "kill"));
	}

	/**
	 * Sends a console command to a service by given ID
	 * @param id Service ID
	 * @param cmd Console command to send to the service
	 * @return Map
	 * @throws IOException
	 */
	public Map consoleCommand(String id, String cmd) throws IOException
	{
		return request(POST, address(id, "msg"), new AbstractMap.SimpleEntry<>("msg", cmd));
	}

	/**
	 * Get all co-op-vault donations
	 * @return Map
	 * @throws IOException
	 */
	public Map listAllDonations() throws IOException
	{
		return request(GET, coOpVault());
	}

	/**
	 * Get all co-op-vault donations in the month and year specified
	 * @param month Numerical month 1-12 for donation list
	 * @param year Numerical year. Defaults to current year if null
	 * @return Map
	 * @throws IOException
	 */
	public Map listDonationsByMonth(int month, Integer year) throws IOException
	{
		Map.Entry[] entries = new Map.Entry[year == null ? 1 : 2];
		entries[0] = new AbstractMap.SimpleEntry<>("month", month);
		if (year != null)
		{
			entries[1] = new AbstractMap.SimpleEntry<>("year", year);
		}
		return request(POST, coOpVault(), entries);
	}
}
