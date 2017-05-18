package cs.jniwrap.webcrawl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;
import org.jsoup.*;
import org.jsoup.nodes.Document;

public abstract class WebCrawler {
	//protected String[] pageSrc;
	protected Document pageDoc;
	/*
	 * used for navigating through the hrefs
	 */
	protected String baseURL;
	public WebCrawler(String sUrl) {
		try {
			pageDoc = Jsoup.connect(sUrl).get();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		baseURL = sUrl.substring(0, sUrl.lastIndexOf('/') + 1);
	}
	
	public abstract PackageEntry[] listPackages();
	
	

}
