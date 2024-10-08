package com.craftinginterpreters.lox;

import java.util.List;

class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void>
{
    private Environment environment = new Environment();
    private boolean isBroken = false;
    private int isInBlock = 0;

    void interpret(List<Stmt> statements)
    {
        try
        {
            for (Stmt statement : statements)
            {
                execute(statement);
            }
        } catch (RuntimeError error)
        {
            Lox.runtimeError(error);
        }
    }

    private void execute(Stmt stmt)
    {
        stmt.accept(this);
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt)
    {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    void executeBlock(List<Stmt> statements, Environment environment)
    {
        Environment previous = this.environment;
        try
        {
            this.environment = environment;
            isInBlock++;

            for (Stmt statement : statements)
            {
                if (isBroken)
                    break;
                execute(statement);
            }
        } finally
        {
            this.environment = previous;
            isInBlock--;
        }
    }

    private String stringify(Object object)
    {
        if (object == null)
            return "nil";

        if (object instanceof Double)
        {
            String text = object.toString();
            if (text.endsWith(".0"))
            {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }

        return object.toString();
    }

    private Object evaluate(Expr expr)
    {
        return expr.accept(this);
    }


    @Override
    public Void visitIfStmt(Stmt.If stmt)
    {
        if (isTruthy(evaluate(stmt.condition)))
        {
            execute(stmt.thenBranch);
        } else if (stmt.elseBranch != null)
        {
            execute(stmt.elseBranch);
        }
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt)
    {
        while (isTruthy(evaluate(stmt.condition)) && !isBroken)
        {
            execute(stmt.body);
        }
        isBroken = false;
        return null;
    }

    @Override
    public Void visitBreakStmt(Stmt.Break stmt)
    {
        if (isInBlock <= 0)
            throw new RuntimeError(stmt.operator, "Break statement outside of loop.");
        isBroken = true;
        return null;
    }


    @Override
    public Void visitPrintStmt(Stmt.Print stmt)
    {
        Object value = evaluate(stmt.expression);
        if (stmt.expression instanceof Expr.Variable)
            checkVarIsInitialized((Expr.Variable) stmt.expression);
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt)
    {
        Object value = null;
        if (stmt.initializer != null)
        {
            value = evaluate(stmt.initializer);
        }

        environment.define(stmt.name.lexeme, value);
        return null;
    }


    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt)
    {
        Object value = evaluate(stmt.expression);
        if (!Lox.isInFile)
            System.out.println(stringify(value));
        return null;
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr)
    {
        return expr.value;
    }

    @Override
    public Object visitLogicalExpr(Expr.Logical expr)
    {
        Object left = evaluate(expr.left);

        if (expr.operator.type == TokenType.OR)
        {
            if (isTruthy(left))
                return left;
        } else
        {
            if (!isTruthy(left))
                return left;
        }

        return evaluate(expr.right);
    }


    @Override
    public Object visitVariableExpr(Expr.Variable expr)
    {
        return environment.get(expr.name);
    }


    @Override
    public Object visitGroupingExpr(Expr.Grouping expr)
    {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr)
    {
        Object value = evaluate(expr.value);
        environment.assign(expr.name, value);
        return value;
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr)
    {
        Object right = evaluate(expr.right);
        if (expr.right instanceof Expr.Variable)
            checkVarIsInitialized((Expr.Variable) expr.right);

        switch (expr.operator.type)
        {
            case BANG:
                return !isTruthy(right);
            case MINUS:
                checkNumberOperand(expr.operator, right);
                return -(double) right;
        }

        // Unreachable.
        assert (false);
        return null;
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr)
    {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);
        if (expr.left instanceof Expr.Variable)
            checkVarIsInitialized((Expr.Variable) expr.left);
        if (expr.right instanceof Expr.Variable)
            checkVarIsInitialized((Expr.Variable) expr.right);

        switch (expr.operator.type)
        {
            case GREATER:
                checkNumberOperands(expr.operator, left, right);
                return (double) left > (double) right;
            case GREATER_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double) left >= (double) right;
            case LESS:
                checkNumberOperands(expr.operator, left, right);
                return (double) left < (double) right;
            case LESS_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double) left <= (double) right;
            case MINUS:
                checkNumberOperands(expr.operator, left, right);
                return (double) left - (double) right;
            case BANG_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return !isEqual(left, right);
            case EQUAL_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return isEqual(left, right);
            case PLUS:
                if (left instanceof Double && right instanceof Double)
                {
                    return (double) left + (double) right;
                }
                if (left instanceof String && right instanceof String)
                {
                    return (String) left + (String) right;
                }

                if (left instanceof String && right instanceof Double)
                {
                    return (String) left + stringify(right);
                }
                if (left instanceof Double && right instanceof String)
                {
                    return stringify(left) + (String) right;
                }
                throw new RuntimeError(expr.operator, "Operands must be two numbers or two strings.");
            case SLASH:
                if ((double) right == 0)
                    throw new RuntimeError(expr.operator, "Divide by zero.");
                return (double) left / (double) right;
            case STAR:
                return (double) left * (double) right;
            case COMMA:
                return right;
        }

        // Unreachable.
        assert (false);
        return null;
    }

    @Override
    public Object visitThreeWayExpr(Expr.ThreeWay expr)
    {
        Object judge = evaluate(expr.judge);
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        if (expr.judge instanceof Expr.Variable)
            checkVarIsInitialized((Expr.Variable) expr.judge);
        if (expr.left instanceof Expr.Variable)
            checkVarIsInitialized((Expr.Variable) expr.left);
        if (expr.right instanceof Expr.Variable)
            checkVarIsInitialized((Expr.Variable) expr.right);

        if (isTruthy(judge))
            return left;
        else
            return right;
    }

    private void checkVarIsInitialized(Expr.Variable var)
    {
        if (environment.get(var.name) == null)
        {
            throw new RuntimeError(var.name, "Uninitialized variable '" + var.name.lexeme + "'.");
        }
    }


    private void checkNumberOperand(Token operator, Object operand)
    {
        if (operand instanceof Double)
            return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    private void checkNumberOperands(Token operator, Object left, Object right)
    {
        if (left instanceof Double && right instanceof Double)
            return;

        throw new RuntimeError(operator, "Operands must be numbers.");
    }

    private boolean isTruthy(Object object)
    {
        if (object == null)
            return false;
        if (object instanceof Boolean)
            return (boolean) object;
        return true;
    }

    private boolean isEqual(Object a, Object b)
    {
        if (a == null && b == null)
            return true;
        if (a == null)
            return false;

        return a.equals(b);
    }
}