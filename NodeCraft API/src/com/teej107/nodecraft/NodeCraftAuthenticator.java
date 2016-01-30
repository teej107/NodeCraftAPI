package com.teej107.nodecraft;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.Arrays;

/**
 * @author teej107
 * @since Jan 30, 2016
 */
public class NodeCraftAuthenticator extends Authenticator
{
	private String username;
	private char[] apiKey;

	public NodeCraftAuthenticator(String username, char[] apiKey)
	{
		this.username = username;
		this.apiKey = apiKey;
	}

	@Override
	protected PasswordAuthentication getPasswordAuthentication()
	{
		PasswordAuthentication pa = new PasswordAuthentication(username, apiKey);
		Arrays.fill(apiKey, (char) 0);
		return pa;
	}
}
