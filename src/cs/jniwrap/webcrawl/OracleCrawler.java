package cs.jniwrap.webcrawl;

import java.util.List;
import java.util.Vector;

import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.NodeVisitor;

import cs.jniwrap.webcrawl.WebCrawler;

public class OracleCrawler extends WebCrawler {
	PackageEntry[] m_packageList;
	public OracleCrawler() {
		super("https://docs.oracle.com/javase/8/docs/api/overview-summary.html");
		m_packageList = buildPackageList();
	}
	
	public Node findTable(Node n){
		Node result = null;
		for(Node node : n.childNodes()) {
			if(node.nodeName().contains("table")) {
				result = node;
				break;
			}
			if(node.childNodes() != null && !node.childNodes().isEmpty()) {
				Node innerResult = findTable(node);
				if(innerResult != null) {
					result = innerResult;
					break;
				}
			}
				
		}
		return result;
	}
	/*
	public void nodeRecurse(Node node) {
		for(Node n : node.childNodes()) {
			System.out.println(n.nodeName() + n.attributes());
			if(n.childNodes() != null && !n.childNodes().isEmpty())
				nodeRecurse(n);
		}
	}*/
	
	private PackageEntry[] buildPackageList() {
		Node tableNode = findTable(pageDoc);
		class PackageRootFinder implements NodeVisitor {
			boolean isNextTBody = false;
			public Node result = null;
			@Override
			public void head(Node arg0, int arg1) {
				if(arg0.hasAttr("scope"))
					isNextTBody = true;
				if(arg0.nodeName() == "tbody" && isNextTBody)
					result = arg0;
			}

			@Override
			public void tail(Node arg0, int arg1) {
				
			}
			
		}
		PackageRootFinder finder = new PackageRootFinder();
		
		tableNode.traverse(finder);
		Node packageRoot = finder.result;
		
		final Vector<String> descriptions = new Vector<>();
		final Vector<String> packageNames = new Vector<>();
		final Vector<String> packageUrls = new Vector<>();
		
		Traverse.traverseHead(packageRoot, outerNode -> {
			if(outerNode.nodeName().equals("tr"))
				Traverse.traverseHead(outerNode, innerNode -> {
				if(innerNode.nodeName().equals("a") && innerNode.parent().nodeName().equals("td")) {
					Attributes a = innerNode.attributes();
					Element e  = (Element)innerNode;
					a.forEach(attr -> {
						String key = attr.getKey();
						if(key.equals("href"))
							packageUrls.add(attr.getValue());
						System.out.println(attr.getKey() + " " + attr.getValue());
					});
					if(e.hasText())
						packageNames.add(e.ownText());
					//System.out.println(innerNode);
				}
				else if(innerNode.nodeName().equals("td")) {
					boolean isColLast = false;
					for(Attribute a : innerNode.attributes()) {
						if(a.getKey().equals("class") && a.getValue().equals("colLast")) {
							isColLast = true;
							break;
						}
							
					}
					if(isColLast) {
						Node divNode = Traverse.traverseHeadResult(innerNode, 
								innestNode -> innestNode.nodeName().equals("div"));
						if(divNode == null)
							descriptions.add("No description available for this package.");
						else {
							Element e = (Element) divNode;
							if(e.hasText())
								descriptions.add(e.ownText());
							
						}
					}
				}
			});
		});
		
		if(descriptions.size() == packageNames.size() && packageNames.size() == packageUrls.size()) {
			PackageEntry[] packages = new PackageEntry[packageNames.size()];
			for(int i = 0; i < packages.length; ++i)
				packages[i] = new PackageEntry(packageNames.get(i), packageUrls.get(i), descriptions.get(i));
			return packages;
		}
		
		return null;
	}
	@Override
	public PackageEntry[] listPackages() {
		return m_packageList;
	}

}
