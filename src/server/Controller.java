package server;

import data.Data;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.net.*;
import java.util.Enumeration;
import java.util.ResourceBundle;

public class Controller implements Initializable
{

	private StringProperty text = new SimpleStringProperty(this, "text", "Welcome\n");
	private Socket socket;
	private Socket incomingfile;
	private ServerSocket server;
	private ObjectInputStream objectInputStream;
	private ObjectOutputStream objectOutputStream;
	private FileOutputStream fileOutputStream;
	private FileInputStream fileInputStream;
	private Data inputData;
	private Data outputData;
	private Stage primaryStage;
	File inputFile;
	File outputFile;
	File parent = null;
	private Thread connectThread = null;
	private Thread listenThread;
	private Thread textthread;
	private BufferedReader bufferedReader;
	private PrintWriter printWriter;

	@FXML Label log;
	@FXML TextField saveDirectory;
	@FXML TextField filePath;
	@FXML TextField ipText;
	@FXML TextField portText;
	@FXML Button startButton;
	@FXML Button sendButton;
	@FXML Button browseButton;
	@FXML Button connectButton;

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		log.textProperty().bind(text);
	}

	public void onStartButtonClicked()
	{
		connectThread = new Thread(() ->
		{
			try
			{
				//Platform.runlater() is used to modify ui from a different thread
				Platform.runLater(() ->text.set("Starting server...\n"+ text.get()));

				server = new ServerSocket(4444);
				Platform.runLater(() ->{
					try
					{
						text.set("Server started at port "+ server.getLocalPort() +" on "+ InetAddress.getLocalHost() +"\n"+ text.get());
					} catch (Exception e)
					{
						e.printStackTrace();
					}
					startButton.setText("Stop server");
					startButton.onMouseClickedProperty().setValue((event) -> onStopButtonClicked());
					connectButton.disableProperty().setValue(true);
				});

				socket = server.accept();
				objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
				Platform.runLater(() ->{
					text.set("Connection accepted.\n"+ text.get());
					browseButton.disableProperty().setValue(false);
					sendButton.disableProperty().setValue(false);
				});

				try
				{
					incomingfile = server.accept();
					printWriter = new PrintWriter(incomingfile.getOutputStream());
					bufferedReader = new BufferedReader(new InputStreamReader(incomingfile.getInputStream()));
					textthread = new Thread(()->{
						try{
							String ready;
							while(true){
								ready = "";
								try
								{
									ready = bufferedReader.readLine();
									if(!ready.equals(""))
										Platform.runLater(() ->text.set("Receiving file.\n"+ text.get()));
								} catch (IOException e)
								{
									e.printStackTrace();
									break;
								}
							}
						}catch (Exception e)
						{
							e.printStackTrace();
						}
					});
					textthread.start();
				} catch (IOException e)
				{
					e.printStackTrace();
				}

				try
				{
					objectInputStream = new ObjectInputStream(socket.getInputStream());
					while ((inputData = (Data) objectInputStream.readObject()) != null)
					{
						Platform.runLater(() ->text.set("Incoming file....\n"+ text.get()));
						if (parent == null)
							parent = new File(saveDirectory.getText());
						inputFile = new File(parent.getPath() +"\\"+ inputData.getName());
						//Platform.runLater(() ->text.set(text.get() + inputFile.getPath() +"\n"));
						fileOutputStream = new FileOutputStream(inputFile);
						fileOutputStream.write(inputData.getFile());
						Platform.runLater(() ->text.set(inputData.getName() +" recieved.\n" + text.get()));
						fileOutputStream.close();
					}
					objectInputStream.close();
				}catch (IOException e)
				{
					Platform.runLater(() -> onStopButtonClicked());
					e.printStackTrace();
				} catch (ClassNotFoundException e)
				{
					Platform.runLater(() ->text.set("Class not found.\n" + text.get()));
					e.printStackTrace();
				}
			} catch(UnknownHostException e){
				e.printStackTrace();
			}
			catch (IOException e) {
				Platform.runLater(() ->text.set("Server could not start.\n"+ text.get()));
				e.printStackTrace();
			}
		});
		connectThread.start();
	}

	public void onConnectButtonClicked()
	{
		try{
			text.set("Connecting to server...\n"+ text.get());
			if(ipText.getText() != null && !portText.getText().equals(""))
			{
				socket = new Socket(InetAddress.getByName(ipText.getText().trim()), Integer.parseInt(portText.getText().trim()));
				incomingfile = new Socket(InetAddress.getByName(ipText.getText().trim()), Integer.parseInt(portText.getText().trim()));
			}
			else
			{
				socket = new Socket(InetAddress.getLocalHost(), 4444);
				incomingfile = new Socket(InetAddress.getLocalHost(), 4444);
			}
			printWriter = new PrintWriter(incomingfile.getOutputStream());
			objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
			text.set("Connected to server.\n"+ text.get());
			connectButton.setText("Disconnect");
			connectButton.onMouseClickedProperty().setValue(event -> onDisconnectButtonClicked());
			startButton.disableProperty().setValue(true);
			sendButton.disableProperty().setValue(false);
			browseButton.disableProperty().setValue(false);
			listenThread = new Thread(()->
			{
				try
				{
					objectInputStream = new ObjectInputStream(socket.getInputStream());
					while ((inputData = (Data) objectInputStream.readObject()) != null)
					{
						Platform.runLater(() -> text.set("Incoming file....\n" + text.get()));
						if (parent == null)
							parent = new File(saveDirectory.getText());
						inputFile = new File(parent.getPath() + "\\" + inputData.getName());
						//Platform.runLater(() ->text.set(text.get() + inputFile.getPath() +"\n"));
						fileOutputStream = new FileOutputStream(inputFile);
						fileOutputStream.write(inputData.getFile());
						Platform.runLater(() -> text.set(inputData.getName() + " recieved.\n" + text.get()));
						fileOutputStream.close();
					}
					objectInputStream.close();
				} catch (IOException e)
				{
					Platform.runLater(() -> onDisconnectButtonClicked());
					e.printStackTrace();
				} catch (ClassNotFoundException e)
				{
					Platform.runLater(() -> text.set("Class not found.\n" + text.get()));
					e.printStackTrace();
				}
			});
		}catch (UnknownHostException e){
			e.printStackTrace();
			text.set("Cannot connect to server. Check IP and port no.\n"+ text.get());
		}catch (IOException e){
			e.printStackTrace();
			text.set("Couldn't connect to server\n"+ text.get());
		}
		listenThread.start();
		try
		{
			bufferedReader = new BufferedReader(new InputStreamReader(incomingfile.getInputStream()));
			textthread = new Thread(()->{
				try{
					String ready;
					while((ready = bufferedReader.readLine()) != null){
						Platform.runLater(() ->text.set("Receiving file.\n"+ text.get()));
					}
				}catch (Exception e)
				{
					e.printStackTrace();
				}
			});
			textthread.start();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public void onSendButtonClicked()
	{
		new Thread(()->{
			try
			{
				Platform.runLater(()->text.set("Sending file...\n"+ text.get()));
				printWriter.println("Sending...");
				outputData = new Data();
				if (outputFile == null)
					outputFile = new File(filePath.getText());
				outputData.setPath(outputFile.getPath());
				outputData.setName(outputFile.getName());

				fileInputStream = new FileInputStream(outputFile);
				byte bytearray[] = new byte[fileInputStream.available()];
				fileInputStream.read(bytearray);
				outputData.setFile(bytearray);

				if(objectOutputStream == null)
					objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
				objectOutputStream.writeObject(outputData);
				Platform.runLater(()->text.set(outputData.getName() +" sent.\n"+ text.get()));

				objectOutputStream.flush();
				fileInputStream.close();
			} catch (IOException e)
			{
				text.set("Error sending file.\n"+ text.get());
				e.printStackTrace();
			}
		}).start();
	}

	public void onStopButtonClicked()
	{
		try
		{
			connectThread.interrupt();
			textthread.interrupt();
			if(socket != null)
				socket.close();
			server.close();
			text.set("Server stopped.\n" + text.get());
			startButton.setText("Start Server");
			startButton.onMouseClickedProperty().setValue((event) -> onStartButtonClicked());
			sendButton.disableProperty().setValue(true);
			browseButton.disableProperty().setValue(true);
			connectButton.disableProperty().setValue(false);
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public void onDisconnectButtonClicked()
	{
		try
		{
			text.set("Disconnecting from server...\n"+ text.get());
			listenThread.interrupt();
			textthread.interrupt();
			socket.close();
			text.set("Disconnected from server.\n"+ text.get());
			connectButton.setText("Connect");
			connectButton.onMouseClickedProperty().setValue((event) -> onConnectButtonClicked());
			sendButton.disableProperty().setValue(true);
			browseButton.disableProperty().setValue(true);
			startButton.disableProperty().setValue(false);
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public void onSaveButtonClicked()
	{
		DirectoryChooser directoryChooser = new DirectoryChooser();
		directoryChooser.setTitle("Save files to");
		parent = directoryChooser.showDialog(primaryStage);
		Platform.runLater(() ->{
			//text.set(text.get() + parent.getPath() +"\n");
			saveDirectory.setText(parent.getPath());
		});
	}

	public void onBrowseButtonClicked()
	{
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Select file to send");
		outputFile = fileChooser.showOpenDialog(primaryStage);
		if (outputFile != null){
			filePath.setText(outputFile.getPath());
		}
	}

	StringProperty textProperty()
	{
		return text;
	}

	public void setSocket(Socket socket){ this.socket = socket;	}

	public Stage getPrimaryStage()
	{
		return primaryStage;
	}

	public void setPrimaryStage(Stage primaryStage)
	{
		this.primaryStage = primaryStage;
	}
}
