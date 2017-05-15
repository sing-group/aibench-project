/*
 * #%L
 * The AIBench Shell Plugin
 * %%
 * Copyright (C) 2006 - 2017 Daniel Glez-Peña and Florentino Fdez-Riverola
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
package bsh;

// Just testing...

/**
 * Implementation of the enhanced for(:) statement. This statement uses
 * BshIterable to support iteration over a wide variety of iterable types. Under
 * JDK 1.1 this statement supports primitive and Object arrays, Vectors, and
 * enumerations. Under JDK 1.2 and later it additionally supports collections.
 * 
 * @author Daniel Leuck
 * @author Pat Niemeyer
 */
class BSHEnhancedForStatement extends SimpleNode implements ParserConstants {
        String varName;

        BSHEnhancedForStatement(int id) {
                super(id);
        }

        public Object eval(CallStack callstack, Interpreter interpreter) throws EvalError {
                Class elementType = null;
                SimpleNode expression, statement = null;

                NameSpace enclosingNameSpace = callstack.top();
                SimpleNode firstNode = ((SimpleNode) jjtGetChild(0));
                int nodeCount = jjtGetNumChildren();

                if (firstNode instanceof BSHType) {
                        elementType = ((BSHType) firstNode).getType(callstack, interpreter);
                        expression = ((SimpleNode) jjtGetChild(1));
                        if (nodeCount > 2)
                                statement = ((SimpleNode) jjtGetChild(2));
                } else {
                        expression = firstNode;
                        if (nodeCount > 1)
                                statement = ((SimpleNode) jjtGetChild(1));
                }

                BlockNameSpace eachNameSpace = new BlockNameSpace(enclosingNameSpace);
                callstack.swap(eachNameSpace);

                final Object iteratee = expression.eval(callstack, interpreter);

                if (iteratee == Primitive.NULL)
                        throw new EvalError("The collection, array, map, iterator, or " + "enumeration portion of a for statement cannot be null.", this, callstack);

                CollectionManager cm = CollectionManager.getCollectionManager();
                if (!cm.isBshIterable(iteratee))
                        throw new EvalError("Can't iterate over type: " + iteratee.getClass(), this, callstack);
                BshIterator iterator = cm.getBshIterator(iteratee);

                Object returnControl = Primitive.VOID;
                while (iterator.hasNext()) {
                        try {
                                if (elementType != null)
                                        eachNameSpace.setTypedVariable(varName/* name */, elementType/* type */, iterator.next()/* value */, new Modifiers()/* none */);
                                else
                                        eachNameSpace.setVariable(varName, iterator.next(), false);
                        } catch (UtilEvalError e) {
                                throw e.toEvalError("for loop iterator variable:" + varName, this, callstack);
                        }

                        boolean breakout = false; // switch eats a
                        // multi-level break
                        // here?
                        if (statement != null) // not empty statement
                        {
                                Object ret = statement.eval(callstack, interpreter);

                                if (ret instanceof ReturnControl) {
                                        switch (((ReturnControl) ret).kind) {
                                        case RETURN:
                                                returnControl = ret;
                                                breakout = true;
                                                break;

                                        case CONTINUE:
                                                break;

                                        case BREAK:
                                                breakout = true;
                                                break;
                                        }
                                }
                        }

                        if (breakout)
                                break;
                }

                callstack.swap(enclosingNameSpace);
                return returnControl;
        }
}
