package cs.jniwrap.webcrawl;

import org.jsoup.nodes.Node;
import org.jsoup.select.NodeVisitor;

public final class Traverse {
	@FunctionalInterface
	public interface HeadTraverser {
		public void head(Node node);
	}
	@FunctionalInterface
	public interface HeadTraverserResult {
		public boolean head(Node node);
	}
	public static void traverseHead(final Node node, final HeadTraverser trav) {
		class DummyVisitor implements NodeVisitor {

			@Override
			public void head(Node arg0, int arg1) {
				trav.head(arg0);
				
			}

			@Override
			public void tail(Node arg0, int arg1) {
				// TODO Auto-generated method stub
				
			}
			
		}
		node.traverse(new DummyVisitor());
	}
	
	public static Node traverseHeadResult(final Node node, final HeadTraverserResult trav) {
		class DummyVisitor implements NodeVisitor {
			public Node m_result;
			@Override
			public void head(Node arg0, int arg1) {
				if(m_result == null)
					if(trav.head(arg0))
						m_result = arg0;
				
			}

			@Override
			public void tail(Node arg0, int arg1) {
				// TODO Auto-generated method stub
				
			}
			
		}
		final DummyVisitor visitor = new DummyVisitor();
		node.traverse(visitor);
		return visitor.m_result;
	}
}
