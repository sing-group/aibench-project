/*
 * #%L
 * The AIBench Shell Plugin
 * %%
 * Copyright (C) 2006 - 2016 Daniel Glez-Peña and Florentino Fdez-Riverola
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */
/**
 Display the contents of the current working directory.  
 The format is similar to the Unix ls -l
 <em>This is an example of a bsh command written in Java for speed.</em>
 
 @method void dir( [ String dirname ] )
 */
package bsh.commands;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import bsh.CallStack;
import bsh.Interpreter;
import bsh.StringUtil;

public class dir {
        static final String[] months = { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };

        public static String usage() {
                return "usage: dir( String dir )\n       dir()";
        }

        /**
         * Implement dir() command.
         */
        public static void invoke(Interpreter env, CallStack callstack) {
                String dir = ".";
                invoke(env, callstack, dir);
        }

        /**
         * Implement dir( String directory ) command.
         */
        public static void invoke(Interpreter env, CallStack callstack, String dir) {
                File file;
                try {
                        file = env.pathToFile(dir);
                } catch (IOException e) {
                        env.println("error reading path: " + e);
                        return;
                }

                if (!file.exists() || !file.canRead()) {
                        env.println("Can't read " + file);
                        return;
                }
                if (!file.isDirectory()) {
                        env.println("'" + dir + "' is not a directory");
                }

                String[] files = file.list();
                files = StringUtil.bubbleSort(files);

                for (int i = 0; i < files.length; i++) {
                        File f = new File(dir + File.separator + files[i]);
                        StringBuffer sb = new StringBuffer();
                        sb.append(f.canRead() ? "r" : "-");
                        sb.append(f.canWrite() ? "w" : "-");
                        sb.append("_");
                        sb.append(" ");

                        Date d = new Date(f.lastModified());
                        GregorianCalendar c = new GregorianCalendar();
                        c.setTime(d);
                        int day = c.get(Calendar.DAY_OF_MONTH);
                        sb.append(months[c.get(Calendar.MONTH)] + " " + day);
                        if (day < 10)
                                sb.append(" ");

                        sb.append(" ");

                        // hack to get fixed length 'length' field
                        int fieldlen = 8;
                        StringBuffer len = new StringBuffer();
                        for (int j = 0; j < fieldlen; j++)
                                len.append(" ");
                        len.insert(0, f.length());
                        len.setLength(fieldlen);
                        // hack to move the spaces to the front
                        int si = len.toString().indexOf(" ");
                        if (si != -1) {
                                String pad = len.toString().substring(si);
                                len.setLength(si);
                                len.insert(0, pad);
                        }

                        sb.append(len.toString());

                        sb.append(" " + f.getName());
                        if (f.isDirectory())
                                sb.append("/");

                        env.println(sb.toString());
                }
        }
}
