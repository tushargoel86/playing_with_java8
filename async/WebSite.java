import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class WebSite {

	private String head;
	
	private String meta;
	
	private String title;

	
	@XmlElement
	public String getHead() {
		return head;
	}



	@XmlElement
	public String getMeta() {
		return meta;
	}


	
	@XmlElement
	public String getTitle() {
		return title;
	}


	

	public void setHead(String head) {
		this.head = head;
	}



	public void setMeta(String meta) {
		this.meta = meta;
	}



	public void setTitle(String title) {
		this.title = title;
	}



	@Override
	public String toString() {
		return "WebSite [head=" + head + ", meta=" + meta + ", title=" + title + "]";
	}

	
	
}
