package coap.mediator;
import java.net.URI;

import org.eclipse.californium.core.CoapResponse;

// The generic class that represents a COAP request
public abstract class CoapRequest {
	
	private CoapRequestID id;
	private CoapResponse response;

	// ResponseReady = false 						-> response not available yet (some errors occurred)
	// ResponseReady = true and response  = null 	-> no response available
	// ResponseReady = true and response != null 	-> response available
	private boolean responseReady;
	
	protected CoapRequest(int id, String uri){
		this.id = new CoapRequestID(id, uri);
		responseReady = false;
		response = null;
	}
	
	protected void SetResponse(CoapResponse response){
		this.response = response;
		responseReady = true;
	}
	
	public boolean IsResponseReady(){
		return responseReady;
	}
	
	public CoapRequestID GetRequestId() {
		return id;
	}

	public int getNumericId(){
		return id.getNumericId();
	}
	
	public URI GetUri() {
		return id.getUri();
	}

	public CoapResponse GetResponse() {
		return response;
	}

}
