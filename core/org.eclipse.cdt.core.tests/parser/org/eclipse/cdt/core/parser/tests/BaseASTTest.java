/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.core.parser.tests;

import java.io.StringReader;
import java.util.Iterator;

import junit.framework.TestCase;

import org.eclipse.cdt.core.parser.IParser;
import org.eclipse.cdt.core.parser.IQuickParseCallback;
import org.eclipse.cdt.core.parser.ParserFactory;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ast.IASTCompilationUnit;
import org.eclipse.cdt.core.parser.ast.IASTDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTFunction;
import org.eclipse.cdt.core.parser.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTSimpleTypeSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTVariable;
import org.eclipse.cdt.internal.core.parser.ParserException;
import org.eclipse.cdt.internal.core.parser.ScannerInfo;

/**
 * @author jcamelon
 *
 */
public class BaseASTTest extends TestCase
{
	public BaseASTTest( String a )
	{
		super( a );
	}
	
	protected IQuickParseCallback quickParseCallback; 
	protected IParser parser; 
	protected IASTCompilationUnit parse( String code, boolean quick, boolean throwExceptionOnError ) throws ParserException
	{
		ParserMode mode = quick ? ParserMode.QUICK_PARSE : ParserMode.COMPLETE_PARSE; 
		quickParseCallback = ParserFactory.createQuickParseCallback(); 
		parser = ParserFactory.createParser( ParserFactory.createScanner( new StringReader( code ), "code", new ScannerInfo(), mode), quickParseCallback, mode );
		if( ! parser.parse() && throwExceptionOnError )
			throw new ParserException("Parse failure");
		return quickParseCallback.getCompilationUnit(); 
	}
	
	protected IASTCompilationUnit parse( String code )throws ParserException
	{
		return parse( code, true, true );
	}
	
	protected IASTDeclaration assertSoleDeclaration( String code ) throws ParserException
	{
		Iterator declarationIter = parse( code ).getDeclarations();
		assertTrue( declarationIter.hasNext() );
		IASTDeclaration returnValue = (IASTDeclaration)declarationIter.next();
		assertFalse( declarationIter.hasNext() );
		return returnValue;
	}
	
	public void assertCodeFailsParse(String code) {
		boolean testPassed = false;
		try {
			IASTCompilationUnit tu = parse(code);
			testPassed = true;
			fail( "We should not reach this point");
		} catch (Throwable e) {
			if (!(e instanceof ParserException))
				fail("Unexpected Error: " + e.getMessage());
		}
		if (testPassed)
			fail("The expected error did not occur.");
	}

    protected void assertSimpleReturnType(IASTFunction function, IASTSimpleTypeSpecifier.Type type)
    {
    	assertEquals( ((IASTSimpleTypeSpecifier)function.getReturnType().getTypeSpecifier()).getType(), type ); 
    }
    
	protected void assertSimpleType(IASTVariable variable, IASTSimpleTypeSpecifier.Type type)
	{
		assertEquals( ((IASTSimpleTypeSpecifier)variable.getAbstractDeclaration().getTypeSpecifier()).getType(), type ); 
	}
	
	protected void assertParameterSimpleType(IASTParameterDeclaration variable, IASTSimpleTypeSpecifier.Type type)
	{
		assertEquals( ((IASTSimpleTypeSpecifier)variable.getTypeSpecifier()).getType(), type ); 
	}

    protected void failedAsExpected()
    {
        assertFalse( "The expected error did not occur.", false );
    }

    protected void assertNotReached()
    {
        fail( "We should not reach this point");
    }

    protected void assertQualifiedName(String [] fromAST, String [] theTruth)
    {
    	assertNotNull( fromAST );
    	assertNotNull( theTruth );
    	assertEquals( fromAST.length, theTruth.length );
    	for( int i = 0; i < fromAST.length; ++i )
    	{
    		assertEquals( fromAST[i], theTruth[i]);
    	}
    }

}
