package my.test;

import java.io.BufferedReader;
import java.io.FileReader;

import my.thrift.SendData;

import org.apache.log4j.Logger;

import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.TProcessor;
import org.apache.thrift.TProcessorFactory;
import org.apache.thrift.protocol.TBinaryProtocol;


public class JavaServer {
	private static final Logger LOG = Logger.getLogger(JavaServer.class);

	private static int localPort = 9090;
	private static int linesPerRPC = 1;
	private static String testFile;

	public static MyDistributor dataDist = new MyDistributor();

	public static void main(String[] args) {

		if (args.length < 2) {
			System.out.println("Usage: JavaServer port file [Lines per RPC]");
			System.exit(0);
		}
		try {
			localPort = Integer.parseInt(args[0]);
			testFile = args[1];
			if (args.length > 2) {
				linesPerRPC = Integer.parseInt(args[2]);
			}
		} catch (Exception e) {
			LOG.warn(e.getMessage());
			System.exit(0);
		}
		dataDist.linesPerRPC = linesPerRPC;

		try {

			new Thread(dataDist).start();

			Runnable SendDataThread = new Runnable() {
				@Override
				public void run() {
					sendData();
				}
			};

			Runnable simple = new Runnable() {
				@Override
				public void run() {
					simpleServer();
				}
			};

			new Thread(SendDataThread).start();

			// start server and block here ...
			new Thread(simple).start();

		} catch (Exception x) {
			LOG.warn(x.getMessage());
		}
	}

	public static void simpleServer() {
		try {

			TProcessorFactory processorFactory = new TProcessorFactory(null) {
				@Override
				public TProcessor getProcessor(TTransport trans) {
					LOG.info("add client connection.");
					LOG.info("client connection class: "
							+ trans.getClass().getName());
					dataDist.addClient(new SendData.Client(new TBinaryProtocol(
							trans)));
					return new SendData.Processor(new SendDataHandler());
				}
			};

			TServerTransport serverTransport = new TServerSocket(localPort);

			TServer.Args serverArgs = new TServer.Args(serverTransport);
			serverArgs.processorFactory(processorFactory);

			// TServer server = new TSimpleServer(new
			// Args(serverTransport).processor(processor));
			TServer server = new TSimpleServer(serverArgs);
			// TServer server = new TThreadPoolServer(
			// new
			// TThreadPoolServer.Args(serverTransport).processor(processor));

			LOG.info("Starting the simple server...");
			server.serve();
		} catch (Exception e) {
			LOG.warn(e.getMessage());
		}
	}

	public static void sendData() {
		try {
			String line = "";
			BufferedReader input = new BufferedReader(new FileReader(testFile));

			long counter = 0;
			long startTime = System.currentTimeMillis();
			LOG.info("Start Time (ms) : " + startTime);
			while ((line = input.readLine()) != null) {
				dataDist.addData(line);
				counter++;
			}
			long endTime = System.currentTimeMillis();
			LOG.info("End Time (ms) : " + endTime);
			LOG.info("Elapsed Time (ms) : " + (endTime - startTime));
			LOG.info(counter + " lines are added to sending queue.");

			input.close();
		} catch (Exception e) {
			LOG.error(e.getMessage());
		}
	}

}
