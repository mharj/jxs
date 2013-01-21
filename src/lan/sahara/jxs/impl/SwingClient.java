package lan.sahara.jxs.impl;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;

import lan.sahara.jxs.common.GContext;
import lan.sahara.jxs.common.Window;

public class SwingClient extends AbsApiClient {
	HashMap<Integer,JFrame> windowList = new HashMap<Integer,JFrame>();
	public SwingClient() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public Boolean clientCreateWindow(final Window window) {
		Rectangle geometry = window.getGeometry();
		JFrame frame = new JFrame("JXS Demo");
		int bg_color=( window.getAttribute(Window.AttrBackgroundPixel) | 0xff000000 );
//		System.out.println("BG Color:"+new Color(bg_color).toString());
//		frame.setBackground(Color.DARK_GRAY);
		frame.setBackground(new Color(bg_color));
		frame.setSize(geometry.width, geometry.height);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		frame.addWindowListener(new WindowAdapter() {
			@Override
		    public void windowClosing(WindowEvent windowEvent) {
				SwingClient.this.clientDestroyWindow(window);
		    }
		});
		windowList.put(window.getId(), frame);
		return true;
	}

	@Override
	public Boolean clientCreateGC(GContext gc) {
		int parent_id = gc.getParentId();
		JPanel swingGc = new JPanel();
		swingGc.setBackground(Color.BLUE);
		// attach to Window if we have one
		if ( windowList.containsKey(parent_id) ) {
			JFrame parent = windowList.get(parent_id);
			Dimension d = parent.getSize();
			swingGc.setSize(d);
			
//			parent.add(swingGc);
			parent.add(new MyPanel(d,gc));
			
			parent.pack();
/*			System.out.println("SwingClient: clientCreateGC() JPanel attached to ID:"+parent_id);
			Graphics g = swingGc.getGraphics();
			if ( g != null ) {
				g.setColor(Color.RED);
				g.drawLine(10, 10, 20, 20);
			}*/

		}
		return null;
	}
	private class MyPanel extends JPanel {

	    public MyPanel(Dimension preferredSize,GContext gc) {
	    	setPreferredSize(preferredSize);
	    	
	    	setBackground( new Color( gc.getAttribute(GContext.AttrBackground) ) ) ;
//	    	setForeground( new Color( gc.getAttribute(GContext.AttrForeground) ) ) ;
	    	
	        setBorder(BorderFactory.createLineBorder(Color.black));
	    }
/*
	    public Dimension getPreferredSize() {
	        return new Dimension(250,200);
	    }
*/
	    public void paintComponent(Graphics g) {
	        super.paintComponent(g);       

	        // Draw Text
	        g.drawString("This is my custom Panel!",10,20);
	    }  
	}
	@Override
	public Boolean clientFreeResource(Integer id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int clientGetInputFocus() {
		// TODO return current focus window
		return 0;
	}


}
