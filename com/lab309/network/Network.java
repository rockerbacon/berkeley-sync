package com.lab309.network;

import android.net.DhcpInfo;
import android.net.wifi.WifiManager;

import com.lab309.general.SizeConstants;

import java.io.IOException;
import java.net.InetAddress;

/**
 * Class for managing information on the currently connected network
 *
 * Created by Vitor Andrade dos Santos on 3/22/17.
 */

public class Network {
	/*ATTRIBUTES*/
	private WifiManager wifi;

	/*CONSTRUCTORS*/
	public Network (WifiManager manager) {
		this.wifi = manager;
	}

	/*GETTERS*/
	public String getName () {
		return this.wifi.getConnectionInfo().getSSID();
	}

	public InetAddress getBroadcasterAddress () throws IOException {
		DhcpInfo dhcp = this.wifi.getDhcpInfo();
		int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
		byte[] bytes = new byte[SizeConstants.sizeOfInt];
		for (int i = 0; i < SizeConstants.sizeOfInt; i++) {
			bytes[i] = (byte)(broadcast >> i*Byte.SIZE);
		}
		return InetAddress.getByAddress(bytes);
	}
}
