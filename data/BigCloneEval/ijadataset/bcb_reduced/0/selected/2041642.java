// Copyright 2005 Konrad Twardowski
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.makagiga.commons;

import static org.makagiga.commons.UI._;

import java.awt.Desktop;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.AccessController;
import java.security.Policy;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;

import org.makagiga.commons.annotation.ConfigEntry;
import org.makagiga.commons.annotation.Uninstantiable;
import org.makagiga.commons.io.MProcess;
import org.makagiga.commons.swing.MMessage;

/**
 * A set of OS dependent functions.
 *
 * @mg.example
 * <pre class="brush: java">
 * public static final void main(String[] args) {
 *   if (OS.isLinux())
 *     System.out.println("This OS is good");
 * ...
 * </pre>
 */
public final class OS {

	// public
	
	/**
	 * @since 2.0
	 */
	@ConfigEntry(value = "OS.openCommand", platform = true)
	public static final StringProperty openCommand = new StringProperty(null, StringProperty.SECURE_WRITE);

	// private

	private static boolean gnome;
	private static boolean kde;
	private static boolean linux;
	private static boolean lxde;
	private static boolean mac;
	private static boolean noDistroInfo;
	private static boolean openJDK;
	private static boolean solaris;
	private static boolean unity;
	private static boolean unix;
	private static boolean webStart;
	private static boolean windows;
	private static boolean xdgError;
	private static boolean xfce;
	private static float _javaVersion;
	private static int _javaUpdate;
	private static int pid;
	private static Locale locale;
	private static Map<String, Path> xdgCache;
	private static String arch;
	private static String archRawProperty;
	private static String distroInfoCache;
	private static String internalName;
	private static String name;
	private static String runtimeName;
	private static String userName;
	private static String version;

	// public

	static {
		init();
	}

	/**
	 * @since 2.0
	 */
	public static boolean email(final String address) {
		if (tryDefaultLaunch(Desktop.Action.MAIL, URI.create("mailto:" + TK.escapeURL(address))))
			return true;
		
		return email(address, null, null);
	}
	
// FIXME: 3.0: problem with encoding/non-ascii chars
	/**
	 * @since 2.0
	 */
	public static boolean email(final String address, final String subject, final File attachment) {
		Path xdgEmailFile = locateXdgUtil("email");
		if (xdgEmailFile != null) {
			StringList args = new StringList();
			args.add(xdgEmailFile.toString());
			args.add("--utf8");
			if (subject != null) {
				args.add("--subject");
				args.add(subject);
			}
			if (attachment != null) {
				args.add("--attach");
				args.add(attachment.getPath());
			}
			if (address != null)
				args.add(address);
			else
				args.add("mailto:");
			
			return tryLaunch(args.toArray());
		}
		else {
			MMessage.error(UI.windowFor(null), _("Could not send mail: {0}", address));
			
			return false;
		}
	}

	/**
	 * Returns the supported architecture.
	 *
	 * Supported return values:
	 * - "x86"
	 * - "x86_64"
	 * - "unknown"
	 *
	 * @since 3.8
	 */
	public static String getArch() { return arch; }

	/**
	 * Returns the OS internal name.
	 *
	 * Supported values are:
	 * - Linux - <code>"linux"</code>
	 * - Mac OS X - <code>"mac"</code>
	 * - Windows - <code>"windows"</code>
	 * - Others - <code>"unknown"</code>
	 */
	public static String getInternalName() { return internalName; }

	/**
	 * Returns the Java Update number.
	 *
	 * EXAMPLES: 0, 1, etc
	 * 
	 * @return @c 0 if update number is unavailable.
	 *
	 * @since 2.0
	 */
	public static int getJavaUpdate() { return _javaUpdate; }

	/**
	 * Returns the Java version number (example: 1.7f).
	 *
	 * @mg.example
	 * <pre class="brush: java">
	 * if (OS.getJavaVersion() >= 1.7f) { // note "f" at the end
	 *    useJDK7Feature();
	 * }
	 * </pre>
	 *
	 * @since 2.0
	 */
	public static float getJavaVersion() { return _javaVersion; }
	
	public static Locale getLocale() {
		return (locale == null) ? Locale.getDefault() : locale;
	}

	/**
	 * Returns the OS name. (e.g. "Linux")
	 */
	public static String getName() { return name; }

	/**
	 * Returns the PID (Process ID) of this application.
	 *
	 * @return Zero if method failed
	 *
	 * @see <a href="http://golesny.de/wiki/code:javahowtogetpid">Java How to get the PID from a process?</a>
	 * @see <a href="http://bugs.sun.com/view_bug.do?bug_id=4244896">Bug #4244896</a>
	 *
	 * @since 3.4
	 */
	public synchronized static int getPID() {
		if (runtimeName == null) {
			RuntimeMXBean rt = ManagementFactory.getRuntimeMXBean();
			runtimeName = rt.getName();
			Tuple.Two<String, String> s = Tuple.split(runtimeName, '@', TK.SPLIT_PAIR_NULL_ERROR);
			if (s != null) {
				try {
					pid = Integer.parseInt(s.get1());
				}
				catch (NumberFormatException exception) {
					MLogger.exception(exception);
				}
			}
		}

		return pid;
	}

	/**
	 * @since 4.0
	 */
	public static String getSummary(final boolean includeDistroInfo) {
		MStringBuilder result = new MStringBuilder();
		
		// application
		result.append(MApplication.getTitle());
		if (!TK.isEmpty(MApplication.getBuildInfo())) {
			result
				.append(" [")
				.append(MApplication.getBuildInfo())
				.append(']');
		}
		result.n();
		
		String codename = MApplication.getResourceString("Application.x.codename", null);
		if (!TK.isEmpty(codename))
			result.formatLine("Codename: %s", codename);
		
		result.n();

		// os
		result
			.append(getName())
			.append(' ')
			.append(getVersion())
			.append(" (").append(archRawProperty).append(')');
		if (isWebStart())
			result.append(", Web Start");
		if (isGNOME())
			result.append(", GNOME");
		if (isKDE())
			result.append(", KDE");
		if (isLXDE())
			result.append(", LXDE");
		if (isUnity())
			result.append(", Unity");
		if (isXfce())
			result.append(", Xfce");
		result.n();
		
		Map<String, String> properties = AccessController.doPrivileged(new PrivilegedAction<Map<String, String>>() {
			@Override
			public Map<String, String> run() {
				HashMap<String, String> map = new HashMap<>();
				map.put("java.runtime.version", System.getProperty("java.runtime.version"));
				map.put("java.vendor", System.getProperty("java.vendor"));
				
				RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
				map.put("class-path", runtime.getClassPath());
				map.put("input-args", TK.toString(runtime.getInputArguments(), ", "));
				map.put("lib-path", runtime.getLibraryPath());
				map.put("policy", Objects.toString(Policy.getPolicy(), "<none>"));
				map.put("vm-name", runtime.getVmName());
				
				return map;
			}
		} );

		result
			// jvm
			.formatLine(
				"Java(tm) Runtime Environment %s (vendor: %s)",
				properties.get("java.runtime.version"),
				properties.get("java.vendor")
			)
			.n()
			.formatLine("JVM Name: %s", properties.get("vm-name"))
			.formatLine("JVM Arguments: %s", properties.get("input-args"))
			.formatLine("Class Path: %s", properties.get("class-path"))
			.formatLine("Library Path: %s", properties.get("lib-path"))
			.n()
			// locale
			.formatLine(
				"Locale: %s, %s (%s)",
				locale.getDisplayLanguage(),
				locale.getDisplayCountry(),
				locale
			)
			.formatLine(
				"Security Manager: %s (policy=%s)",
				System.getSecurityManager(),
				properties.get("policy")
			);

		// ui
		result.formatLine("Toolkit: %s", Toolkit.getDefaultToolkit().getClass().getName());

		LookAndFeel laf = UIManager.getLookAndFeel();
		if (laf != null) {
			result.format("UI: %s (%s", laf.getName(), laf.getClass().getName());
			if (UI.isSynth())
				result.append(", Synth-based");
			result.appendLine(")");
		}
		
		// dirs
		result.n();
		result.appendLine(_("Configuration & Data Directory:"));
		result.appendLine(FS.getConfigDir());
		if (!isWebStart()) {//!!!
			result.n().appendLine(_("Application Directory:"));
			result.appendLine(FS.getBaseDir());
		}
		if (FS.getProfile() != null) {
			result.n().appendLine(_("Profile: {0}", FS.getProfile()));
		}

		if (includeDistroInfo) {
			String distroInfo = AccessController.doPrivileged(new PrivilegedAction<String>() {
				@Override
				public String run() {
					return getDistroInfo();
				}
			} );
			if (distroInfo != null) {
				result
					.n()
					.appendLine("Distribution Info (lsb_release):")
					.appendLine(distroInfo);
			}
		}
		
		return result.toString();
	}

	/**
	 * Returns the current user name. (e.g. "konrad")
	 */
	public static String getUserName() { return userName; }

	/**
	 * Returns the OS version. (e.g. "5.1" on Windows XP)
	 */
	public static String getVersion() { return version; }

	/**
	 * Initializes this class.
	 *
	 * @mg.note Since 3.8.2, this method is invoked automatically.
	 */
	public static void init() {
		initJavaVersion();
		initArch();

		name = System.getProperty("os.name");
		userName = System.getProperty("user.name");
		version = System.getProperty("os.version");
		// detect os
		String osName = TK.toUpperCase(name);
		if (osName.startsWith("LINUX"))
			linux = true;
		else if (osName.startsWith("MAC"))
			mac = true;
		else if (osName.startsWith("SUNOS"))
			solaris = true;
		else if (osName.startsWith("WINDOWS"))
			windows = true;

		if (linux) {
			internalName = "linux";
			
			// detect desktop session

			// check for KDE
			String desktopSession = System.getenv("DESKTOP_SESSION");
			kde =
				(desktopSession != null) &&
				TK.containsIgnoreCase(desktopSession, "KDE");
			if (!kde) {
				String kdeFullSession = System.getenv("KDE_FULL_SESSION");
				kde = (kdeFullSession != null) && kdeFullSession.equalsIgnoreCase("true");

				if (!kde) {
					gnome = (desktopSession != null) && TK.containsIgnoreCase(desktopSession, "GNOME");
					if (!gnome) {
						unity = (desktopSession != null) && TK.containsIgnoreCase(desktopSession, "UBUNTU");
						if (unity)
							gnome = true;
					}

					// check for Xfce
					// FIX #2988499 - always check "desktopSession" for null
					if (!gnome) {
						xfce = (desktopSession != null) && TK.containsIgnoreCase(desktopSession, "XFCE");
						if (!xfce) {
							lxde = (desktopSession != null) && TK.containsIgnoreCase(desktopSession, "LXDE");
						}
					}
				}
			}
		}
		else if (mac) {
			internalName = "mac";
		}
		else if (solaris) {
			internalName = "solaris";
		}
		else if (windows) {
			internalName = "windows";
		}
		else {
			internalName = "unknown";
		}
		unix = !windows;
		webStart = System.getProperty("javawebstart.version") != null;
		
		initLocale();
	}

	/**
	 * Returns @c true if @b GNOME is a current session.
	 */
	public static boolean isGNOME() { return gnome; }
	
	/**
	 * @since 3.4
	 */
	public static boolean isHeadless() {
		return GraphicsEnvironment.isHeadless();
	}

	/**
	 * Returns @c true if @b KDE is a current session.
	 */
	public static boolean isKDE() { return kde; }

	/**
	 * Returns @c true if application is running under Linux.
	 */
	public static boolean isLinux() { return linux; }
	
	/**
	 * @since 4.2
	 */
	public static boolean isLXDE() { return lxde; }

	/**
	 * Returns @c true if application is running under Mac OS X.
	 */
	public static boolean isMac() { return mac; }

	/**
	 * Returns {@code true} if application is running under OpenJDK.
	 *
	 * @since 3.8
	 */
	public static boolean isOpenJDK() { return openJDK; }
	
	/**
	 * Returns @c true if application is running under (Open)Solaris.
	 *
	 * @since 3.0
	 */
	public static boolean isSolaris() { return solaris; }
	
	public static boolean isSupported() {
		return linux || mac || solaris || windows;
	}

	/**
	 * @since 4.2
	 */
	public static boolean isUnity() { return unity; }

	public static boolean isUnix() { return unix; }
	
	/**
	 * Returns @c true if application was launched via Web Start.
	 */
	public static boolean isWebStart() { return webStart; }

	/**
	 * Returns @c true if application is running under Windows.
	 */
	public static boolean isWindows() { return windows; }

	/**
	 * Returns {@code true} if <i>Xfce</i> is the current desktop session.
	 *
	 * @return {@code true} if <i>Xfce</i>
	 *
	 * @since 3.8.3
	 */
	public static boolean isXfce() { return xfce; }

// TODO: 2.0: test and improve: mac, solaris
	/**
	 * Launches the web browser/email client/other application with the specified URI.
	 * Displays an error message if launch failed.
	 *
	 * @mg.example
	 * <pre class="brush: java">
	 * OS.open(URI.create("http://www.google.com"));
	 * </pre>
	 *
	 * @param uri An URI to open
	 * 
	 * @return @c true if successful; otherwise @c false
	 * 
	 * @throws NullPointerException If @p uri is @c null
	 *
	 * @see MApplication#openURI(java.net.URI)
	 * 
	 * @since 3.0
	 */
	public static boolean open(final URI uri) {
		Objects.requireNonNull(uri);
		
		if ("mailto".equals(uri.getScheme()))
			return email(uri.getSchemeSpecificPart());
		
		String uriString = uri.toString();
		
		// try custom command
		if (!windows && !openCommand.isEmpty()) {
	 		if (tryLaunch(openCommand.get(), uriString))
				return true;
		}

		// try default browser
		if (tryDefaultLaunch(Desktop.Action.BROWSE, uri))
			return true;
		
		// try xdg-utils
		Path xdgOpenFile = locateXdgUtil("open");

		if ((xdgOpenFile != null) && tryLaunch(xdgOpenFile.toString(), uriString))
			return true;

		MMessage.error(
			UI.windowFor(null),
			_("Could not open: {0}", uri) +
			(windows ? "" : ("\n\n" + "Hint: See menu -> Tools -> Settings -> General\nto configure file browser/launcher."))
		);

		return false;
	}

	// private
	
	@Uninstantiable
	private OS() { }
	
	private static String extractCountry(final String country) {
		int i = country.indexOf('.');
		
		return
			(i == -1)
			? country
			: country.substring(0, i);
	}

	private static String getDistroInfo() {
		synchronized (OS.class) {
			if (noDistroInfo)
				return null;

			if (distroInfoCache != null)
				return distroInfoCache;
		}

		if (!isLinux())
			return null;

		try {
			ProcessBuilder p = new ProcessBuilder();
			p.command("lsb_release", "--codename", "--description", "--id", "--release");
			synchronized (OS.class) {
				distroInfoCache = MProcess.readOutputText(p, StandardCharsets.UTF_8).toString();
			}

			return distroInfoCache;
		}
		catch (InterruptedException exception) {
			MLogger.exception(exception);

			return null;
		}
		catch (IOException exception) {
			MLogger.exception(exception);
			synchronized (OS.class) {
				noDistroInfo = true;
			}

			return null;
		}
	}

	private static void initArch() {
		archRawProperty = System.getProperty("os.arch");
		arch = archRawProperty;
		if (arch == null) {
			arch = "unknown";
		}
		else {
			switch (arch) {
				case "i386":
				case "x86":
					arch = "x86";
					break;
				case "amd64":
				case "x86_64":
					arch = "x86_64";
					break;
				default:
					arch = "unknown";
					break;
			}
		}
	}

	private static void initJavaVersion() {
		String runtimeName = System.getProperty("java.runtime.name");
		openJDK = (runtimeName != null) && runtimeName.contains("OpenJDK");

		String jv = System.getProperty("java.version");

		_javaVersion = 1.7f;
		try {
			int i = jv.lastIndexOf('.');
			if (i != -1)
				_javaVersion = Float.parseFloat(jv.substring(0, i));
		}
		catch (Exception exception) {
			MLogger.exception(exception);
			MLogger.warning("core", "Cannot parse Java Version info: %s", jv);
		}

		_javaUpdate = 0;
		try {
			// "underscore" version - x.y.z_uu -> uu
			int updateIndex = jv.indexOf('_');
			if (updateIndex != -1) {
				int qualifierIndex = jv.indexOf('-');
				String update;
				if (qualifierIndex != -1)
					update = jv.substring(updateIndex + 1, qualifierIndex);
				else
					update = jv.substring(updateIndex + 1, jv.length());
				_javaUpdate = Integer.parseInt(update);
			}
		}
		catch (Exception exception) {
			MLogger.warning("core", "Cannot parse Java Update info: %s", jv);
		}
	}

	@edu.umd.cs.findbugs.annotation.SuppressWarnings("MDM_SETDEFAULTLOCALE")
	private static void initLocale() {
		if (linux) {
			String LANG = System.getenv("MAKAGIGA_LANG");
			if (!TK.isEmpty(LANG)) {
				int i = LANG.indexOf('_');
				String language = null;
				String country = null;
				String variant = null;
				if (i == -1) {
					language = LANG;
					Locale.setDefault(new Locale(language));
				}
				else {
					language = LANG.substring(0, i);
					String country_variant = LANG.substring(i + 1);
					i = country_variant.indexOf('_');
					if (i == -1) {
						country = extractCountry(country_variant);
						Locale.setDefault(new Locale(language, country));
					}
					else {
						country = extractCountry(country_variant.substring(0, i));
						variant = country_variant.substring(i + 1);
						Locale.setDefault(new Locale(language, country, variant));
					}
				}
				// MLogger.debug("core", "language=\"%s\", country=\"%s\", variant=\"%s\"", language, country, variant);
			}
		}
		locale = Locale.getDefault();
	}

	private synchronized static Path locateXdgUtil(final String action) {
		if (xdgError)
			return null;
		
		if (xdgCache == null)
			xdgCache = new HashMap<>();
		
		String commandName = "xdg-" + action;
		Path path = xdgCache.get(action);
		if (path != null) {
			//MLogger.debug("core", "Using cached %s path: %s", commandName, path);

			return path;
		}
		
		InputStream input = OS.class.getResourceAsStream(commandName);
		if (input == null) {
			MLogger.debug("core", "No %s resource found", commandName);
		
			return null;
		}
		
		try {
			path = Files.createTempFile("makagiga-" + commandName, ".sh");
			MLogger.debug("core", "Extracting %s -> %s", commandName, path);
			Files.copy(input, path, StandardCopyOption.REPLACE_EXISTING);
			Files.setPosixFilePermissions(path, PosixFilePermissions.fromString("rwxr--r--")); // +x
			path.toFile().deleteOnExit();
			xdgCache.put(action, path);
		
			return path;
		}
		catch (IOException exception) {
			MLogger.exception(exception);
			xdgError = true;
		
			return null;
		}
		finally {
			FS.close(input);
		}
	}
	
	private static boolean tryDefaultLaunch(final Desktop.Action action, final URI uri) {
		try {
			if (!Desktop.isDesktopSupported())
				return false;
		}
		// bad implementation...
		catch (Exception exception) {
			MLogger.exception(exception);
			
			return false;
		}
		
		Desktop desktop = Desktop.getDesktop();
		if (desktop.isSupported(action)) {
			MLogger.debug("core", "Using default launcher...");
			try {
				switch (action) {
					case BROWSE:
						desktop.browse(uri);
						break;
					case MAIL:
						desktop.mail(uri);
						break;
				}
					
				return true;
			}
			catch (IOException exception) { } // quiet
		}
		
		return false;
	}

	@SuppressWarnings("ResultOfObjectAllocationIgnored")
	private static boolean tryLaunch(final String... args) {
		try {
			new MProcess(args);

			return true;
		}
		catch (IOException exception) {
			MLogger.exception(exception);
			
			return false;
		}
	}

	@SuppressWarnings("ResultOfObjectAllocationIgnored")
	private static boolean tryLaunch(final String command, final String url) {
		try {
			new MProcess(withURL(command, url).toArray());

			return true;
		}
		catch (IOException exception) {
			MLogger.exception(exception);
			
			return false;
		}
	}
		
	private static StringList withURL(final String command, String url) {
		StringList l = new StringList();
		
		url = url.replace(" ", "%20");

// FIXME: space characters in parameters
		if (command.contains("%u")) {
			l.addAll(TK.fastSplit(command.replace("%u", url), ' '));
		}
		else {
			l.addAll(TK.fastSplit(command, ' '));
			l.add(url);
		}

		return l;
	}

}
