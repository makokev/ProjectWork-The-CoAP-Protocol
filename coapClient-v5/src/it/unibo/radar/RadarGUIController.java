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

import coap.mediator.CoapRequestID;
import coap.mediator.MediatorMessage;
import coap.mediator.client.CoapMediatorClient;

public class RadarGUIController extends JFrame {

	private static final long serialVersionUID = 1L;
	private static final String URI_STRING = "coap://localhost:5683/RadarPoint";
	
	private HashMap<Integer, CoapRequestID> requestIDs;
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
		CoapRequestID requestId = CoapMediatorClient.Get(URI_STRING);
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
				CoapRequestID requestId = CoapMediatorClient.Put(URI_STRING, point.compactToString());
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
		
		CoapRequestID requestId = requestIDs.get(id);
		requestIDs.remove(id);
		MediatorMessage message = CoapMediatorClient.GetResponse(requestId);
		txtArea.append(message.getMessage()+"\n");
	}

}


