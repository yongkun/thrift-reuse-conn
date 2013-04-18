package my.test;

import java.util.List;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.FileReader;

import my.thrift.SendData;

import org.apache.log4j.Logger;


import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;

public class JavaClient {
	private static final Logger LOG = Logger.getLogger(JavaClient.class);

	private static String remoteHost = "localhost";
	private static int remotePort = 9090;
	private static int linesPerRPC = 1;
	private static String testFile;

	public static void main(String[] args) {

		if (args.length < 3) {
			System.out.println("Usage: JavaClient remote_host remote_port file "
							+ "[Lines per RPC] [polling interval] [reconnect interval]");
			System.exit(0);
		}
		remoteHost = args[0];
		testFile = args[2];
		int pollingInterval = 100, reconnectInterval = 5000;
		try {
			remotePort = Integer.parseInt(args[1]);
			if (args.length > 3) {
				linesPerRPC = Integer.parseInt(args[3]);
			}
			// deprecated
			if (args.length > 4) {
				pollingInterval = Integer.parseInt(args[4]);
			}
			if (args.length > 5) {
				reconnectInterval = Integer.parseInt(args[5]);
			}
		} catch (Exception e) {
			LOG.warn(e.getMessage());
			System.exit(0);
		}

		SendData.Iface handler = new SendDataHandler();

		try {
			TTransport transport;
			transport = new TSocket(remoteHost, remotePort);
			transport.open();

			TProtocol protocol = new TBinaryProtocol(transport);
			SendData.Client client = new SendData.Client(protocol);

			MyReceiver receiver = new MyReceiver(protocol, handler);
			receiver.pollingInterval = pollingInterval;
			receiver.reconnectInterval = reconnectInterval;
			new Thread(receiver).start();

			// send some data
			perform(client);

			// transport.close();
		} catch (TException x) {
			LOG.warn(x.getMessage());
		}
	}

	private static void perform(SendData.Client client) throws TException {
		try {
			String line = "";
			BufferedReader input = new BufferedReader(new FileReader(testFile));

			List<String> dataList = new ArrayList<String>();
			long counter = 0;

			long startTime = System.currentTimeMillis();
			LOG.info("Start Time (ms) : " + startTime);
			while ((line = input.readLine()) != null) {
				if (linesPerRPC == 1) {
					client.sendOne(line);
				} else {
					dataList.add(line);
					if (counter % linesPerRPC == 0) {
						if (dataList.size() > 0) {
							client.sendList(dataList);
							dataList = new ArrayList<String>();
						}
					}
				}
				counter++;
			}
			if (dataList.size() > 0) {
				client.sendList(dataList);
			}
			long endTime = System.currentTimeMillis();
			LOG.info("End Time (ms) : " + endTime);
			LOG.info("Elapsed Time (ms) : " + (endTime - startTime));
			LOG.info("Records being sent: " + counter);

			input.close();
		} catch (Exception e) {
			LOG.error(e.getMessage());
		}
	}
}
