package ch.ethz.iks.concierge.splash;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Label;
import java.awt.Panel;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

public final class SplashScreenActivator implements BundleActivator {
	private Thread thread;
	private Frame f;

	public void start(final BundleContext context) throws Exception {
		System.out.println("Splash Screen about to start");
		thread = new Thread() {
			public void run() {
				f = new Frame() {
					private static final long serialVersionUID = -669674473757972146L;

					public void show() {
						Dimension size = Toolkit.getDefaultToolkit()
								.getScreenSize();
						int width = size.width < 300 ? size.width : 300;
						int heigth = size.height < 400 ? size.height - 10 : 400;
						setSize(width, heigth);
						addWindowListener(new WindowAdapter() {
							public void windowClosing(final WindowEvent e) {
								// shutdown the framework
								try {
									context.getBundle(0).stop();
								} catch (BundleException be) {
									be.printStackTrace();
								}
							}
						});
						setLayout(new BorderLayout());

						Panel background = new Panel() {
							private static final long serialVersionUID = -7719998254380318187L;

							final Image logo = Toolkit.getDefaultToolkit()
									.getImage(
											context.getBundle().getResource(
													"logo.jpg"));

							public void paint(Graphics g) {
								super.paint(g);
								Dimension size = this.getSize();
								g.setColor(Color.white);
								g.fillRect(getX(), getY(), getWidth(),
										getHeight());
								g
										.drawImage(logo, (size.width - logo
												.getWidth(null)) / 2,
												(size.height - logo
														.getHeight(null)) / 2,
												this);
							}
						};
						add(background, BorderLayout.CENTER);
						background.invalidate();
						background.repaint();

						Panel info = new Panel();
						info.setLayout(new BorderLayout());
						Label label = new Label("Concierge OSGi");
						info.add(label, BorderLayout.NORTH);
						label = new Label(
								"on "
										+ context
												.getProperty("org.osgi.framework.os.name")
										+ " "
										+ context
												.getProperty("org.osgi.framework.os.version"));
						info.add(label, BorderLayout.CENTER);
						label = new Label(
								"architecture: "
										+ context
												.getProperty("org.osgi.framework.processor"));
						info.add(label, BorderLayout.SOUTH);
						add(info, BorderLayout.SOUTH);
						super.show();
					}
				};

				f.show();

				synchronized (this) {
					try {
						wait();
					} catch (InterruptedException e) {
						// we have been interrupted, so let the thread end
					}
				}
			}
		};
		thread.start();
	}

	public void stop(final BundleContext context) throws Exception {
		System.out.println("stopping splash screen.");
		f.hide();
		thread.interrupt();
		thread = null;
	}

}
