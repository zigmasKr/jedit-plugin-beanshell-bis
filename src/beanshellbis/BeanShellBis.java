
/*
 * BeanShellBis.java
 *
 * Copyright (c) 2021 Zigmantas Kryzius <zigmas.kr@gmail.com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.    See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 */

package beanshellbis;

import java.lang.*;
import org.gjt.sp.jedit.*;
import org.gjt.sp.util.Log;

import org.gjt.sp.jedit.bsh.*;
import org.gjt.sp.jedit.bsh.Interpreter;

public class BeanShellBis {
	
	public static Interpreter biFree;
	public static NameSpace nsFree;
	private static View actView;
	
	public BeanShellBis() {
		// new 'free' interpreter:
		biFree = new org.gjt.sp.jedit.bsh.Interpreter();
		nsFree = biFree.getNameSpace();
		actView = null;
		Log.log(Log.NOTICE, BeanShellBis.class, "biFree 1: " + biFree.toString());
		Log.log(Log.NOTICE, BeanShellBis.class, "nsFree 1: " + nsFree.toString());
	}
	
	public void expandNameSpace() {
		nsFree.importPackage("org.gjt.sp.jedit");
		nsFree.importPackage("org.gjt.sp.jedit.buffer");
		nsFree.importPackage("org.gjt.sp.jedit.syntax");
		nsFree.importPackage("org.gjt.sp.jedit.textarea");
		nsFree.importPackage("org.gjt.sp.util");
		//
		nsFree.importPackage("org.gjt.sp.jedit.browser");
		nsFree.importPackage("org.gjt.sp.jedit.bufferset");
		nsFree.importPackage("org.gjt.sp.jedit.statusbar");
		nsFree.importPackage("org.gjt.sp.jedit.gui");
		nsFree.importPackage("org.gjt.sp.jedit.help");
		nsFree.importPackage("org.gjt.sp.jedit.io");
		nsFree.importPackage("org.gjt.sp.jedit.menu");
		nsFree.importPackage("org.gjt.sp.jedit.msg");
		nsFree.importPackage("org.gjt.sp.jedit.options");
		nsFree.importPackage("org.gjt.sp.jedit.pluginmgr");
		nsFree.importPackage("org.gjt.sp.jedit.print");
		nsFree.importPackage("org.gjt.sp.jedit.search");
		nsFree.importPackage("org.jedit.io");
		// for debug purpose:
		Log.log(Log.NOTICE, BeanShellBis.class, "nsFree 2: " + nsFree.toString());
	}
	 
	public static void setVariablesView(View aview) {
		EditPane editPane;
		// variables related to @aview are set only when jEdit's view is changed
		if ((actView == null) || !actView.equals(aview)) {
			editPane = aview.getEditPane();
			try 
			{
				nsFree.setVariable("view", aview);
				nsFree.setVariable("editPane", editPane);
				nsFree.setVariable("buffer", editPane.getBuffer());
				nsFree.setVariable("textArea", editPane.getTextArea());
				nsFree.setVariable("wm", aview.getDockableWindowManager());
				actView = aview;
				// for debug purpose:
				Log.log(Log.DEBUG, BeanShellBis.class, "nsFree 3: " + aview.hashCode());
				Log.log(Log.DEBUG, BeanShellBis.class, "nsFree 3: " + nsFree.toString());
			} catch (UtilEvalError ueErr) {
				Log.log(Log.ERROR, BeanShellBis.class, "UtilEvalError: " + ueErr.toString());
			}
		} else {
			// for debug purpose:
			Log.log(Log.DEBUG, BeanShellBis.class, "nsFree 4: " + aview.hashCode());
		}
			
	}        
	                                                       
	public static BshMethod getBshMethodByName(View vw, String script, String method) {
		Object bshObj = null;
		BshMethod bshm = null;
		setVariablesView(vw);
		try 
		{
			bshObj = biFree.eval(script, nsFree);
			for (int k = 0; k < nsFree.getMethods().length; k++) {
				if (nsFree.getMethods()[k].getName().equals(method)) {
					bshm = nsFree.getMethods()[k];
				}
			}
		}
		catch(EvalError err)
		{
			Log.log(Log.ERROR, BeanShellBis.class, "EvalError: " + err.toString());
		}
		return bshm;
	}
	
	public static RetVal invokeMethodByName(View aview, String script, String method) {
		boolean error = false;
    	Object obj = null;
		BshMethod bshMethod = getBshMethodByName(aview, script, method);
		try {
			obj = bshMethod.invoke(null, biFree);
		}
		catch(EvalError err) {
			error = true;
			Log.log(Log.ERROR, BeanShellBis.class, "EvalError: " + err.toString());
		}
		return new RetVal(obj, null, error, true);
	}
	
	public static RetVal invokeMethodByNameArgs(View aview, String script, String method, Object... args) {
		boolean error = false;
    	Object obj = null;
		BshMethod bshMethod = getBshMethodByName(aview, script, method);
		try {
			obj = bshMethod.invoke(args, biFree);
		}
		catch(EvalError err) {
			error = true;
			Log.log(Log.ERROR, BeanShellBis.class, "EvalError: " + err.toString());
		}
		return new RetVal(obj, null, error, true);
	}
	
	
	//{{{ 
	/** RetVal class
	 * Encapsulates the return value for the invoke... method.
	 */
	public static class RetVal {
		/** Flag set to true if an error dialog has been shown.*/
		public boolean errorShown;
		/** Flag set to true if an error occured */
		public boolean error;
		/** A CharSequence representing the generated output. */
		public CharSequence out;
		/** The object returned by the javascript engine after evaluation. */
		public Object retVal;

		/**
		 * Creates a new RetVal object. This class contains the result of executing a script.
		 *
		 * @param retVal  the result of evaluating the script.
		 * @param out     the output produced by the script.
		 */
		public RetVal(Object retVal, CharSequence out) {
			this(retVal, out, false, true);
		}

		/**
		 * Creates a new RetVal object. This class contains the result of executing a script.
		 *
		 * @param retVal      the result of evaluating the script.
		 * @param out         the output produced by the script.
		 * @param error       true if an error occured while executing the script.
		 * @param errorShown  true if the error
		 */
		public RetVal(Object retVal, CharSequence out, boolean error, boolean errorShown) {
			this.error = error;
			this.out = out;
			this.retVal = retVal;
			this.errorShown = errorShown;
		}
	}//}}}
}