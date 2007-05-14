package ch.ethz.iks.concierge.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Label;
import java.awt.PopupMenu;
import java.awt.TextArea;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleListener;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;

public class ConciergeGUI extends Frame implements BundleListener {

	private static final long serialVersionUID = -4209856605226238830L;

	private final static Toolkit DT = Toolkit.getDefaultToolkit();

	private final Image BACKGROUND;

	private final Image ICON;

	private final Image BUNDLE_ACTIVE;

	private final Image BUNDLE_INSTALLED;

	private final Image BUNDLE_RESOLVED;

	private final Image BUNDLE_STARTING;

	private final Image BUNDLE_STOPPING;

	private final Dimension BUNDLE_ICON_SIZE = new Dimension(50, 60);

	private final static String[] STATES = { "uninstalled", "installed",
			"resolved", "starting", "stopping", "active" };

	private final Image[] ICONS;

	private static Font ICON_FONT = new Font("Arial", Font.ITALIC, 7);

	private Map bundleIcons = new HashMap(0);

	public ConciergeGUI() {
		super("ConciergeGUI");
		Dimension size = DT.getScreenSize();
		final Bundle myBundle = Activator.context.getBundle();
		ICON = DT.createImage(myBundle.getResource("icons/CG_icon.png"));
		BACKGROUND = DT.createImage(myBundle.getResource("icons/CG_logo.jpg"));
		BUNDLE_ACTIVE = DT.createImage(myBundle
				.getResource("icons/bundle_active.png"));
		BUNDLE_INSTALLED = DT.createImage(myBundle
				.getResource("icons/bundle_installed.png"));
		BUNDLE_RESOLVED = DT.createImage(myBundle
				.getResource("icons/bundle_resolved.png"));
		BUNDLE_STARTING = DT.createImage(myBundle
				.getResource("icons/bundle_starting.png"));
		BUNDLE_STOPPING = DT.createImage(myBundle
				.getResource("icons/bundle_stopping.png"));
		ICONS = new Image[] { null, BUNDLE_INSTALLED, BUNDLE_RESOLVED,
				BUNDLE_STARTING, BUNDLE_STOPPING, BUNDLE_ACTIVE };
		final int width = size.width < 240 ? size.width : 240;
		final int height = size.height < 320 ? size.height - 10 : 320;
		setSize(width, height);
		setBackground(Color.white);
		setIconImage(ICON);
		setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));

		addWindowListener(new WindowAdapter() {
			public void windowClosing(final WindowEvent e) {
				try {
					Activator.context.getBundle(0).stop();
				} catch (BundleException e1) {
					showDialog(e1);
					System.exit(1);
				}
			}
		});
		
		this.addComponentListener(new ComponentListener() {
			public void componentHidden(ComponentEvent e) {				
			}

			public void componentMoved(ComponentEvent e) {				
			}

			public void componentResized(ComponentEvent e) {
				ConciergeGUI.this.invalidate();				
				ConciergeGUI.this.repaint();
			}

			public void componentShown(ComponentEvent e) {
			}			
		});

		final Bundle[] bundles = Activator.context.getBundles();
		Activator.context.addBundleListener(this);
		for (int i = 0; i < bundles.length; i++) {
			final BundleIcon icon = new BundleIcon(bundles[i]);
			bundleIcons.put(bundles[i], icon);
			add(icon);
		}

		final PopupMenu menu = new PopupMenu();
		menu.add("Install Bundle");
		menu.add("System Info");
		menu.add("Shutdown Concierge");
		menu.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				final String cmd = e.getActionCommand().intern();
				if (cmd == "Install Bundle") {
					// TODO: also support URLs !!!
					System.out.println("Install Bundle");
					final FileDialog fileDialog = new FileDialog(
							ConciergeGUI.this);
					fileDialog.setVisible(true);
					final String file = fileDialog.getFile();
					final String dir = fileDialog.getDirectory();
					if (file != null) {
						System.out.println(dir + file);
						try {
							Activator.context.installBundle("file:" + dir
									+ file);
						} catch (BundleException be) {
							showDialog(be);
							return;
						}
					}
				} else if (cmd == "System Info") {
					final StringBuffer text = new StringBuffer();
					text.append("Concierge ");
					text.append(Activator.context.getBundle(0).getHeaders()
							.get(Constants.BUNDLE_VERSION));
					text.append("\n\n");
					text.append("os: ");
					text.append(Activator.context
							.getProperty("org.osgi.framework.os.name"));
					text.append(" ");
					text.append(Activator.context
							.getProperty("org.osgi.framework.os.version"));
					text.append("\n");
					text.append("arch: ");
					text.append(Activator.context
							.getProperty("org.osgi.framework.processor"));
					text.append("\n");
					text.append("vm: ");
					text
							.append(Activator.context
									.getProperty("java.vm.vendor"));
					text.append(" ");
					text.append(Activator.context.getProperty("java.vm.name"));
					text.append(" ");
					text.append(Activator.context
							.getProperty("java.vm.version"));
					showDialog(text.toString());
				} else if (cmd == "Shutdown Concierge") {
					try {
						Activator.context.getBundle(0).stop();
					} catch (BundleException be) {
						showDialog(be);
						System.exit(1);
					}
				}
			}

		});
		this.add(menu);

		addMouseListener(new MouseListener() {

			public void mouseClicked(final MouseEvent e) {
				menu.show(e.getComponent(), e.getX(), e.getY());
			}

			public void mouseEntered(final MouseEvent e) {
			}

			public void mouseExited(final MouseEvent e) {
			}

			public void mousePressed(final MouseEvent e) {
			}

			public void mouseReleased(final MouseEvent e) {
			}
		});

	}

	public void paint(final Graphics g) {								
		final Dimension size = getSize();
		g.clearRect(0, 0, size.width, size.height);
		g.drawImage(BACKGROUND, (size.width - BACKGROUND.getWidth(null)) / 2,
				(size.height - BACKGROUND.getHeight(null)) / 2, this);
		super.paint(g);
	}

	public void update(final Graphics g) {
		paint(g);
	}
	
	public void bundleChanged(final BundleEvent event) {
		final int type = event.getType();
		final Bundle bundle = event.getBundle();
		if (type == BundleEvent.INSTALLED) {
			final BundleIcon icon = new BundleIcon(bundle);
			bundleIcons.put(bundle, icon);
			add(icon);
			invalidate();
			validate();
		} else if (type == BundleEvent.UNINSTALLED) {
			final BundleIcon icon = (BundleIcon) bundleIcons.remove(bundle);
			remove(icon);
			invalidate();
			validate();
		} else {
			((BundleIcon) bundleIcons.get(bundle)).repaint();
		}
	}

	private void showDialog(final Throwable t) {
		final StringWriter s = new StringWriter();
		final PrintWriter p = new PrintWriter(s);
		t.printStackTrace(p);
		showDialog(s.toString());
	}

	private void showDialog(final String text) {
		final Dialog dialog = new Dialog(ConciergeGUI.this);
		dialog.setSize(240, 150);
		final TextArea loc = new TextArea();
		loc.setEditable(false);
		loc.setText(text);
		dialog.add(loc);
		dialog.setIconImage(ICON);
		// dialog.pack();
		dialog.setModal(true);
		dialog.addWindowListener(new WindowAdapter() {
			public void windowClosing(final WindowEvent e) {
				dialog.dispose();
			}
		});
		dialog.setVisible(true);
		return;
	}

	private class BundleIcon extends Container {

		private static final long serialVersionUID = -2570755730671986150L;

		private final String name;

		private final Bundle bundle;

		private final Component icon;

		private final PopupMenu bundleMenu = new PopupMenu();

		private int getState() {
			int state = bundle.getState();
			int result = -1;
			while (state > 0) {
				state = state >> 1;
				result++;
			}
			return result;
		}

		public Dimension getPreferredSize() {
			return BUNDLE_ICON_SIZE;
		}

		public Dimension getMinimumSize() {
			return BUNDLE_ICON_SIZE;
		}

		private BundleIcon(final Bundle bundle) {
			final String loc = bundle.getLocation();
			int pos = loc.lastIndexOf("/");
			if (pos == -1) {
				pos = loc.lastIndexOf("\\");
			}
			this.name = pos > -1 ? loc.substring(pos + 1, loc.length()) : loc;
			this.bundle = bundle;
			setLayout(null);
			icon = new Container() {
				private static final long serialVersionUID = -7381567422211751695L;

				public void paint(Graphics g) {
					g.drawImage(ICONS[getState()], 0, 0, this);
					super.paint(g);
				}

			};
			icon.setSize(50, 44);

			bundleMenu.add("info");
			bundleMenu.addSeparator();
			bundleMenu.add("start");
			bundleMenu.add("stop");
			bundleMenu.add("update");
			bundleMenu.addSeparator();
			bundleMenu.add("uninstall");
			bundleMenu.getItem(2).setEnabled(false);

			final ActionListener al = new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					final String cmd = e.getActionCommand().intern();
					try {
						if (cmd == "info") {
							final StringBuffer text = new StringBuffer();
							text.append("BundleID:");
							text.append("\t");
							text.append(bundle.getBundleId());
							text.append("\n");
							text.append("Location:");
							text.append("\t");
							text.append(bundle.getLocation());
							text.append("\n");
							text.append("State:");
							text.append("\t\t");
							text.append(STATES[getState()]);
							text.append("\n\n");
							text.append("Headers:");
							text.append("\n");
							final Dictionary headers = bundle.getHeaders();
							for (Enumeration keys = headers.keys(); keys
									.hasMoreElements();) {
								final String key = (String) keys.nextElement();
								text.append(key);
								text.append(" = ");
								text.append(headers.get(key));
								text.append("\n");
							}
							text.append("\n");
							final ServiceReference[][] svs = new ServiceReference[][] {
									bundle.getRegisteredServices(),
									bundle.getServicesInUse() };

							for (int k = 0; k < 2; k++) {
								if (svs[k] != null && svs[k].length > 0) {
									text
											.append((k == 0 ? "Registered Services:"
													: "Used Services:"));
									text.append("\n");
									for (int i = 0; i < svs[k].length; i++) {
										final String[] cls = (String[]) svs[k][i]
												.getProperty(Constants.OBJECTCLASS);
										text.append("\t\t[");
										text
												.append(svs[k][i]
														.getProperty(Constants.SERVICE_ID));
										text.append("] - ");
										text.append(Arrays.asList(cls));
										text.append("\n");
									}
									text.append("\n");
								}
							}
							showDialog(text.toString());
							return;
						} else if (cmd == "start") {
							bundle.start();
							return;
						} else if (cmd == "stop") {
							bundle.stop();
							return;
						} else if (cmd == "uninstall") {
							bundle.uninstall();
							return;
						} else if (cmd == "update") {
							// TODO: get location
							bundle.update();
							return;
						}
					} catch (final BundleException be) {
						showDialog(be);
					}
				}
			};

			bundleMenu.addActionListener(al);

			icon.add(bundleMenu);
			icon.addMouseListener(new MouseListener() {

				public void mouseClicked(final MouseEvent e) {
					if (bundle.getState() == Bundle.ACTIVE) {
						bundleMenu.getItem(2).setEnabled(false);
						bundleMenu.getItem(3).setEnabled(true);
						bundleMenu.show(e.getComponent(), e.getX(), e.getY());
					} else {
						bundleMenu.getItem(2).setEnabled(true);
						bundleMenu.getItem(3).setEnabled(false);
						bundleMenu.show(e.getComponent(), e.getX(), e.getY());
					}
				}

				public void mouseEntered(final MouseEvent e) {
				}

				public void mouseExited(final MouseEvent e) {
				}

				public void mousePressed(final MouseEvent e) {
				}

				public void mouseReleased(final MouseEvent e) {
				}

			});
			add(icon);

			final Label label = new Label(name);
			if (name.length() <= 12) {
				label.setAlignment(Label.CENTER);
			} else {
				label.setAlignment(Label.LEFT);
			}
			label.setFont(ICON_FONT);
			label.setBounds(0, 44, 50, 10);
			add(label);
		}
	}
}
