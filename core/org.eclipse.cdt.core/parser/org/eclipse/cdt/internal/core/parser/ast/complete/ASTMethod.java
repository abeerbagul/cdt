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
package org.eclipse.cdt.internal.core.parser.ast.complete;

import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
import org.eclipse.cdt.core.parser.ast.IASTAbstractDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTConstructorMemberInitializer;
import org.eclipse.cdt.core.parser.ast.IASTExceptionSpecification;
import org.eclipse.cdt.core.parser.ast.IASTMethod;
import org.eclipse.cdt.core.parser.ast.IASTTemplate;
import org.eclipse.cdt.internal.core.parser.ast.EmptyIterator;
import org.eclipse.cdt.internal.core.parser.pst.IParameterizedSymbol;
import org.eclipse.cdt.internal.core.parser.pst.TypeInfo;

/**
 * @author jcamelon
 *
 */
public class ASTMethod extends ASTFunction implements IASTMethod
{
    private final List constructorChain;
    private final boolean isConstructor;
    private final boolean isPureVirtual;
    private final ASTAccessVisibility visibility;
    private final boolean isDestructor;
    /**
     * @param symbol
     * @param parameters
     * @param returnType
     * @param exception
     * @param startOffset
     * @param nameOffset
     * @param ownerTemplate
     * @param references
     */
    public ASTMethod(IParameterizedSymbol symbol, int nameEndOffset, List parameters, IASTAbstractDeclaration returnType, IASTExceptionSpecification exception, int startOffset, int nameOffset, IASTTemplate ownerTemplate, List references, boolean previouslyDeclared, 
	boolean isConstructor, boolean isDestructor, boolean isPureVirtual, ASTAccessVisibility visibility, List constructorChain, boolean hasFunctionTryBlock, boolean hasVariableArguments )
    {
        super(
            symbol,
            nameEndOffset,
            parameters,
            returnType,
            exception,
            startOffset,
            nameOffset,
            ownerTemplate,
            references, previouslyDeclared, hasFunctionTryBlock, hasVariableArguments );
        this.visibility = visibility; 
        this.isConstructor = isConstructor;
        this.isDestructor = isDestructor;
        this.isPureVirtual = isPureVirtual; 
        this.constructorChain = constructorChain;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTMethod#isVirtual()
     */
    public boolean isVirtual()
    {
        return symbol.getTypeInfo().checkBit( TypeInfo.isVirtual );
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTMethod#isExplicit()
     */
    public boolean isExplicit()
    {
        return symbol.getTypeInfo().checkBit( TypeInfo.isExplicit);
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTMethod#isConstructor()
     */
    public boolean isConstructor()
    {
        return isConstructor;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTMethod#isDestructor()
     */
    public boolean isDestructor()
    {
        return isDestructor;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTMethod#isConst()
     */
    public boolean isConst()
    {
        return symbol.getTypeInfo().checkBit( TypeInfo.isConst);
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTMethod#isVolatile()
     */
    public boolean isVolatile()
    {
        return symbol.getTypeInfo().checkBit( TypeInfo.isVolatile );
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTMethod#isPureVirtual()
     */
    public boolean isPureVirtual()
    {
        return isPureVirtual;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTMember#getVisiblity()
     */
    public ASTAccessVisibility getVisiblity()
    {
        return visibility;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#acceptElement(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void acceptElement(ISourceElementRequestor requestor)
    {
        try
        {
            requestor.acceptMethodDeclaration(this);
        }
        catch (Exception e)
        {
            /* do nothing */
        }
        methodCallbacks(requestor);
    }
    protected void methodCallbacks(ISourceElementRequestor requestor)
    {
        functionCallbacks(requestor);
        processConstructorChain(requestor);
    }
    
    protected void processConstructorChain(ISourceElementRequestor requestor)
    {
        if( constructorChain != null )
        {
        	Iterator i = getConstructorChainInitializers(); 
        	while( i.hasNext() )
        	{
        		IASTConstructorMemberInitializer c = (IASTConstructorMemberInitializer)i.next();
        		c.acceptElement(requestor);
        	}
        }
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#enterScope(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void enterScope(ISourceElementRequestor requestor)
    {
		try
        {
            requestor.enterMethodBody(this);
        }
        catch (Exception e)
        {
            /* do nothing */
        }
		methodCallbacks( requestor );
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#exitScope(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void exitScope(ISourceElementRequestor requestor)
    {
        try
        {
            requestor.exitMethodBody( this );
        }
        catch (Exception e)
        {
            /* do nothing */
        }
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTMethod#getConstructorChainInitializers()
     */
    public Iterator getConstructorChainInitializers()
    {
		if( constructorChain == null )
			return new EmptyIterator(); 
        return constructorChain.iterator();
    }
}
