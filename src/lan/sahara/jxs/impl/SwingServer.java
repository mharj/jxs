package lan.sahara.jxs.impl;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.font.FontRenderContext;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.util.Map.Entry;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import lan.sahara.jxs.common.Extension;


public class SwingServer extends AbsApiServer {
//	static Dimension screenSize=Toolkit.getDefaultToolkit().getScreenSize();
	private Font systemFont = null; 
	
	@Override
	public AbsApiClient createClient() {
//		System.out.println(""+this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
		return new SwingClient();
	}

	@Override
	public Extension queryExtension(String extension) {
//		System.out.println(""+this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
		return null;
	}

	@Override
	public Rectangle getRootWindowSize() {
//		System.out.println(""+this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
		Rectangle bounds = new Rectangle(0,0,1024,768); // some default size
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			bounds = ge.getMaximumWindowBounds();			
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Root Window size:"+bounds.toString());
		return bounds;
	}

	@Override
	public lan.sahara.jxs.common.Font getDefaultFont() {
		System.out.println(""+this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
		systemFont = new Font( "Monospaced", Font.PLAIN, 12 );
		lan.sahara.jxs.common.Font ret = new lan.sahara.jxs.common.Font(null,toX11Font(systemFont));
		FontRenderContext frc = new FontRenderContext(systemFont.getTransform(), true, true);
		Rectangle2D bounds =  new TextLayout("AAAA", systemFont, frc).getBounds();
		System.out.println(bounds.toString());
		bounds =  new TextLayout(".", systemFont, frc).getBounds();
		System.out.println(bounds.toString());
		return ret;
	}
	private String toX11Font(Font f) {
		String ret="";
		ret+="-swing";
		ret+="-"+f.getName().toLowerCase();
		ret+="-"+(f.isPlain()?"medium":"bold");
		ret+="-"+(f.isItalic()?"i":"r");
		ret+="-normal";
		ret+="-*";
		ret+="-*";
		ret+="-"+f.getSize();
		ret+="-*";
		ret+="-*";
		ret+="-"+(f.getName().toLowerCase().contains("mono")?"m":"p");
		ret+="-*";
		ret+="-iso8859";
		ret+="-1";
		return ret;
	}
}
