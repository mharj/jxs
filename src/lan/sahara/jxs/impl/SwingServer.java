package lan.sahara.jxs.impl;

import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import lan.sahara.jxs.common.Extension;
import lan.sahara.jxs.common.Font;

public class SwingServer extends AbsApiServer {

	@Override
	public AbsApiClient createClient(int clientIdBase, int clientIdStep) {
		System.out.println(""+this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
		return new SwingClient(clientIdBase,clientIdStep);
	}

	@Override
	public Extension queryExtension(String extension) {
		System.out.println(""+this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
		return null;
	}

	@Override
	public Rectangle getRootWindowSize() {
		System.out.println(""+this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
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
	public Font getDefaultFont() {
		System.out.println(""+this.getClass().getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
		return new Font(null,"", this, null);
	}

}
