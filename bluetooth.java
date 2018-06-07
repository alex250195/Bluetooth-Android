import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.app.Activity;
import android.bluetooth.*;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.widget.TextView;
import android.widget.Toast;



public class Principal extends Activity implements
TextToSpeech.OnInitListener {

	
	private static final int REQUEST_ENABLE_BT = 0;
	private static final boolean DEBUG = false;
	private static final String TAG = "BluGate";
	private static final int VOICE_RECOGNITION_REQUEST_CODE = 1001;
	BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    BluetoothSocket mmSocket;
    BluetoothDevice mmDevice;
    OutputStream mmOutputStream;
    InputStream mmInputStream;
    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    volatile boolean stopWorker;

    
    TextView textoVoz ;
    private TextToSpeech tts;
     
    
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.principal);
		View btnPortao1 = findViewById(R.id.portao1);
		View btnPortao2 = findViewById(R.id.portao2);
		View btnDesconecta = findViewById(R.id.btnDesconecta);
		View btnConecta = findViewById(R.id.btnConecta);
		View btnConfig = findViewById(R.id.btnConfig);
		final View btnVoz = findViewById(R.id.btnVoz);
		textoVoz =  (TextView) findViewById(R.id.txtResposta);
			   	
		tts = new TextToSpeech(this, this);


		ativaBT();
		try {
			abreConexao();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	
		btnDesconecta.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
									try {
										fechaConexao();
									} catch (IOException | InterruptedException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
                         }
        });
		btnConecta.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {try {
										abreConexao();
									} 
								 catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
            		
 
                         }
        });
		btnPortao1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            
            	
            	         		try {
									abrePortao("1");
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
            		
 
                         }
        });
		btnPortao2.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            
            	
            	         		try {
									abrePortao("2");
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
            		
 
                         }
        });
		
		btnConfig.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            
            	 Intent intent = new Intent(Principal.this, Config.class);  
                 startActivity(intent);  
            	
                         }
        });
	
		btnVoz.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            
            	speak(btnVoz);
            	
            	
                         }
        });

	}
	
	
	
	
	void abrePortao(String portao) throws InterruptedException {
		
		try {
	        enviaDados(portao);
	        Thread.sleep(500);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	
	void ativaBT()
    {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null)
        {
           Log.e(TAG,"Nenhum adaptador disponível");
        }
        
        if(!mBluetoothAdapter.isEnabled())
        {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 0);
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if(pairedDevices.size() > 0)
        {
            for(BluetoothDevice device : pairedDevices)
            {
                if(device.getName().equals("HC-06")) 
                {
                    mmDevice = device;
                    break;
                }
            }
        }
        Log.e(TAG,"Bluetooth Encontrado:"+ mmDevice.toString());
    }
    
    void abreConexao() throws IOException
    {
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); //Standard SerialPortService ID
        mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);        

        	//mBluetoothAdapter.cancelDiscovery();
        	mmSocket.connect();
        	mmOutputStream = mmSocket.getOutputStream();
        	mmInputStream = mmSocket.getInputStream();
        	Log.e(TAG,"Conexão Bluetooth Aberta");
        	
        }
    
    void escutaDados()
    {
        final Handler handler = new Handler(); 
        final byte delimiter = 10; //This is the ASCII code for a newline character
        
        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        workerThread = new Thread(new Runnable()
        {
            public void run()
            {                
               while(!Thread.currentThread().isInterrupted() && !stopWorker)
               {
                    try 
                    {
                        int bytesAvailable = mmInputStream.available();                        
                        if(bytesAvailable > 0)
                        {
                            byte[] packetBytes = new byte[bytesAvailable];
                            mmInputStream.read(packetBytes);
                            for(int i=0;i<bytesAvailable;i++)
                            {
                                byte b = packetBytes[i];
                                if(b == delimiter)
                                {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;
                                    
                                    handler.post(new Runnable()
                                    {
                                        public void run()
                                        {
                                           Log.e(TAG,data);
                                        }
                                    });
                                }
                                else
                                {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    } 
                    catch (IOException ex) 
                    {
                        stopWorker = true;
                    }
               }
            }
        });

        workerThread.start();
    }
    
    void enviaDados(String dados) throws IOException
    {
    	Log.e(TAG,"Enviando:"+dados);
        String msg = dados;
        msg += "\n";
        try {
        mmOutputStream.write(msg.getBytes());
        Log.e(TAG,"Enviado:"+dados);
        }
        catch(Exception e) { Log.e(TAG,"Erro ao enviar dados:"+e.toString());
        }
        
     
    }
    
    void fechaConexao() throws IOException, InterruptedException
    {
        //stopWorker = true; 
    	try{
        mmOutputStream.close();
        mmInputStream.close();
        Thread.sleep(1000);
        mmSocket.close();
        Log.e(TAG,"Conexão Bluetooth Fechada");}
    	catch(Exception e) {Log.e(TAG,"Erro ao fechar a conexão:"+e.toString());}
    
    }
    
    
    public void checkVoiceRecognition() {
    	  // Check if voice recognition is present
    	  PackageManager pm = getPackageManager();
    	  List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(
    	    RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
    	  if (activities.size() == 0) {
    	 //  mbtSpeak.setEnabled(false);
    	   textoVoz.setText("Reconhecimento de voz não presente");
    	   Toast.makeText(this, "Sem reconhecimento de voz",
    	     Toast.LENGTH_SHORT).show();
    	  }
    	 }
    	 
    	 public void speak(View view) {
    	  Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
    	 
    	  // Specify the calling package to identify your application
    	  intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass()
    	    .getPackage().getName());
    	 
    	  // Display an hint to the user about what he should say.
    	 intent.putExtra(RecognizerIntent.EXTRA_PROMPT, textoVoz.getText().toString());
    	 
    	  // Given an hint to the recognizer about what the user is going to say
    	  //There are two form of language model available
    	  //1.LANGUAGE_MODEL_WEB_SEARCH : For short phrases
    	  //2.LANGUAGE_MODEL_FREE_FORM  : If not sure about the words or phrases and its domain.
    	intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
    	    RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
    	 
    	  int noOfMatches = 1; // Integer.parseInt(msTextMatches.getSelectedItem()
    	  //  .toString());
    	  // Specify how many results you want to receive. The results will be
    	  // sorted where the first result is the one with higher confidence.
    	  intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, noOfMatches);
    	  //Start the Voice recognizer activity for the result.
    	  startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
    	 }
    	 
    	 @Override
    	 protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	  if (requestCode == VOICE_RECOGNITION_REQUEST_CODE)
    	 
    	   //If Voice recognition is successful then it returns RESULT_OK
    	   if(resultCode == RESULT_OK) {
    		   ArrayList<String> textMatchList = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
    		
    		   
    	    if (!textMatchList.isEmpty()) {
    	    	 if (textMatchList.get(0).contains("portão 1")) {
    	    		 
    	    		 textoVoz.setText(textMatchList.get(0).toString());
    	    		 try {
						abrePortao("1");
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
  	    		 
 
    	    		 } else if (textMatchList.get(0).contains("portão 2")) {
        	    		 
        	    		 textoVoz.setText(textMatchList.get(0).toString());
        	    		 try {
     						abrePortao("2");
     					} catch (InterruptedException e) {
     						// TODO Auto-generated catch block
     						e.printStackTrace();
     					}
        	    		 
        	    		 } else {
    	    			 
    	    			 textoVoz.setText("Comando não reconhecido: "+textMatchList.get(0).toString());

    	        	    		 fala(textoVoz.getText().toString());
    	    		 }
    	    }
    	   //Result code for various error.
    	   }else if(resultCode == RecognizerIntent.RESULT_AUDIO_ERROR){
    	    showToastMessage("Erro no áudio");
    	   }else if(resultCode == RecognizerIntent.RESULT_CLIENT_ERROR){
    	    showToastMessage("Erro na Aplicação");
    	   }else if(resultCode == RecognizerIntent.RESULT_NETWORK_ERROR){
    	    showToastMessage("Erro na Rede");
    	   }else if(resultCode == RecognizerIntent.RESULT_NO_MATCH){
    	    showToastMessage("Palavra não identificada");
    	   }else if(resultCode == RecognizerIntent.RESULT_SERVER_ERROR){
    	    showToastMessage("Erro no Servidor");
    	   }
    	  super.onActivityResult(requestCode, resultCode, data);
    	 }
    	 /**
    	 * Helper method to show the toast message
    	 **/
    	 void showToastMessage(String message){
    	  Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    	 }
    	 
    	 public void onDestroy() {
    	        // Don't forget to shutdown tts!
    	        if (tts != null) {
    	            tts.stop();
    	            tts.shutdown();
    	        }
    	        super.onDestroy();
    	    }




		@Override
		public void onInit(int status) {
			 
	        if (status == TextToSpeech.SUCCESS) {
	 
	            int result = tts.setLanguage(Locale.ROOT);
	 
	            if (result == TextToSpeech.LANG_MISSING_DATA
	                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
	                Log.e("TTS", "Language not supported");
	            } else {

	            	
	            }
	 
	        } else {
	            Log.e("TTS", "Erro no Comando de Voz!");
	        }
	 
	    }
		
		private void fala(String texto) {
			 
		 
	        tts.speak(texto, TextToSpeech.QUEUE_FLUSH, null);
	    }
		}
    	 


