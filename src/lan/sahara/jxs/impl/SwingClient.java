package lan.sahara.jxs.impl;

import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import lan.sahara.jxs.common.Window;

public class SwingClient extends AbsApiClient {
	public SwingClient(int resourceIdBase, int resourceIdMask) {
		super(resourceIdBase, resourceIdMask);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Boolean clientCreateWindow(final Window window, Rectangle geometry) {
		System.out.println("SwingClient: clientCreateWindow("+geometry.toString()+")");
		JFrame frame = new JFrame("JXS Demo");
		
		frame.setSize(geometry.width, geometry.height);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		frame.addWindowListener(new WindowAdapter() {
			@Override
		    public void windowClosing(WindowEvent windowEvent) {
				SwingClient.this.clientDestroyWindow(window);
		    }
		});
		return true;
	}
}
