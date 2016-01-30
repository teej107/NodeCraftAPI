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

	public NodeCraft(final String username, char[] apiKey)
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

	public Map listServices() throws IOException
	{
		return request(GET, address(null, null));
	}

	public Map getService(String id) throws IOException
	{
		return request(GET, address(id, null));
	}

	public Map serviceStatus(String id) throws IOException
	{
		return request(GET, address(id, "stats"));
	}

	public Map consoleLogs(String id) throws IOException
	{
		return request(POST, address(id, "logs"));
	}

	public Map consoleInputHistory(String id) throws IOException
	{
		return request(POST, address(id, "history"));
	}

	public Map startServerProcess(String id) throws IOException
	{
		return request(POST, address(id, "start"));
	}

	public Map stopServerProcess(String id) throws IOException
	{
		return request(POST, address(id, "stop"));
	}

	public Map killServerProcess(String id) throws IOException
	{
		return request(POST, address(id, "kill"));
	}

	public Map consoleCommand(String id, String cmd) throws IOException
	{
		return request(POST, address(id, "msg"), new AbstractMap.SimpleEntry<>("msg", cmd));
	}

	public Map listAllDonations() throws IOException
	{
		return request(GET, coOpVault());
	}

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
