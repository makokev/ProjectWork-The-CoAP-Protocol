package it.unibo.radar;

import java.awt.Button;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Label;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.eclipse.californium.core.coap.CoAP.Code;

import com.google.gson.Gson;

import coap.mediator.client.CoapMediatorClient;
import coap.mediator.request.ClientMediatorRequestID;
import coap.mediator.response.CoapMediatorResponse;
import coap.mediator.response.CoapMediatorResponseCode;

public class RadarGUIController extends JFrame {

	private static final long serialVersionUID = 1L;
	private static final String URI_STRING = "coap://localhost:5683/RadarPoint";
	
	private HashMap<Integer, ClientMediatorRequestID> requestIDs;
	private TextField txtDistance, txtAngle, txtResponseId;
	private JTextArea txtArea;
	
	public RadarGUIController(){
		this("Radar GUI Controller");
	}
	
	public RadarGUIController(String title){
		super(title);
		requestIDs = new HashMap<>();
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(500,400);
		this.setResizable(false);
		setLayout(new FlowLayout());
			
		JPanel pnlDistance = new JPanel();
		pnlDistance.setAlignmentX(Component.LEFT_ALIGNMENT);
		Label lable1 = new Label("Distance");
		txtDistance = new TextField(50);
		pnlDistance.add(lable1);
		pnlDistance.add(txtDistance);
		
		JPanel pnlAngle = new JPanel();
		Label lable2 = new Label("Angle");
		txtAngle = new TextField(50);
		pnlAngle.add(lable2);
		pnlAngle.add(txtAngle);
		
		JPanel pnlResponse = new JPanel();
		Label lable3 = new Label("Response id");
		txtResponseId = new TextField(50);
		pnlResponse.add(lable3);
		pnlResponse.add(txtResponseId);
		
		JPanel pnlButton = new JPanel();
		Button btnGet = new Button("GET");
		Button btnPut = new Button("PUT");
		Button btnResponse = new Button("RESPONSE");
		Button btnClear = new Button("CLEAR");
		pnlButton.add(btnGet);
		pnlButton.add(btnPut);
		pnlButton.add(btnResponse);
		pnlButton.add(btnClear);
		
		txtArea = new JTextArea(12,40);
		txtArea.setEditable(false);
		txtArea.setAutoscrolls(true);
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.add(txtArea);
		scrollPane.setViewportView(txtArea);
		
		Container container = getContentPane();
		container.add(pnlDistance);
		container.add(pnlAngle);
		container.add(pnlResponse);
		container.add(pnlButton);
		container.add(scrollPane);
		
		btnGet.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				onRequestGET();
			}
		});	
		btnPut.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				onRequestPUT();
			}
		});
		btnResponse.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				onRequestRESPONSE();
			}
		});
		btnClear.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				txtArea.setText("");
			}
		});	
	
	}
	
	private void onRequestGET(){
		// out format:			GET|uri
		// in  format:			REQUEST_ID|id
		txtArea.append("- GET\n");
		ClientMediatorRequestID requestId = CoapMediatorClient.Get(URI_STRING);
		if(null != requestId){
			requestIDs.put(requestId.getNumericId(), requestId);
			txtArea.append("REQUEST_GET ID: " + requestId.getNumericId()+"\n");		
		}else
			txtArea.append("REQUEST_GET: Response error.\n");
	}
	
	private void onRequestPUT(){
		txtArea.append("- PUT\n");
		String distance = txtDistance.getText();
		String angle = txtAngle.getText();
		if(distance.isEmpty() || angle.isEmpty())
			txtArea.append("Insert data into fields to execute a PUT operation.\n");
		else{
			RadarPoint point = RadarPoint.convertFromString(distance+","+angle);
			if(point != null){
				ClientMediatorRequestID requestId = CoapMediatorClient.Put(URI_STRING, (new Gson()).toJson(point));
				if(null != requestId){
					requestIDs.put(requestId.getNumericId(), requestId);
					txtArea.append("REQUEST_PUT ID: " + requestId.getNumericId()+"\n");					
				} else
					txtArea.append("REQUEST_PUT: Response error.\n");
			}
			else
				txtArea.append("Invalid data inserted (distance: [0,80], angle: [0,360]).\n");
		}
		txtDistance.setText("");
		txtAngle.setText("");
	}

	private void onRequestRESPONSE(){
		// out format:			RESPONSE|uri,requestId
		// in  format:			RESPONSE|SUCCESS,responseText
		// 							or
		// in  format:			RESPONSE|FAILURE,failureMessage
		
		txtArea.append("- RESPONSE\n");
		String responseID = txtResponseId.getText();
		txtResponseId.setText("");
		
		if(responseID.isEmpty()){
			txtArea.append("ERROR: Insert data into the response_id field.\n");
			return;
		}
		
		int id = -1;
		try{
			id = Integer.parseInt(responseID);
		}catch(NumberFormatException e){
			txtArea.append("ERROR: Wrong RequestID format.\n");
			return;
		}
		
		if(!requestIDs.containsKey(id)){
			txtArea.append("ERROR: RequestID does not exist.\n");
			return;
		}
		
		ClientMediatorRequestID requestId = requestIDs.get(id);
		requestIDs.remove(id);
		CoapMediatorResponse response = CoapMediatorClient.GetResponse(requestId);
		if(response.getResponseCode() == CoapMediatorResponseCode.RESPONSE_SUCCESS)
		{
			System.out.println("Response: "+responseID.toString());
			// crash here
			if(response.getRequestId().getRequestType() == Code.GET){
				RadarPoint point = (new Gson()).fromJson(response.getResponseBody(), RadarPoint.class);
				txtArea.append(point.toString()+"\n");
			} else{
				txtArea.append(response.getResponseBody());
			}
		} else{
			txtArea.append("RESPONSE_FAILED: "+response.getResponseBody()+"\n");
		}

	}

}


