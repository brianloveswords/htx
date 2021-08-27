package dev.bjb.htx

import dev.bjb.htx.grammar.ExprLexer
import dev.bjb.htx.grammar.ExprParser
import dev.bjb.htx.grammar.ExprBaseVisitor
import org.antlr.v4.runtime.*
import org.antlr.v4.runtime.tree.*

class ExprParserTest extends CommonSuite:
  test("ok".only) {
    val visitor = ExprVisitor()
    val parser = getParser(
      ExprLexer(_),
      ExprParser(_),
      "(1+2) * 2\n",
    )
    val tree = parser.prog()

    val result = visitor.visit(tree)
    assertEquals(result, 6) //
  }

class ExprVisitor extends ExprBaseVisitor[Int]:
  import ExprParser.*

  var memory = Map[String, Int]()

  override def visitAssign(ctx: AssignContext): Int =
    val id = ctx.ID().getText
    val value = visit(ctx.expr())
    memory += id -> value
    value

  override def visitPrintExpr(ctx: PrintExprContext): Int =
    val value = visit(ctx.expr())
    value

  override def visitInt(ctx: IntContext): Int =
    ctx.INT().getText.toInt

  override def visitId(ctx: IdContext): Int =
    val id = ctx.ID().getText
    memory.getOrElse(id, 0)

  override def visitMulDiv(ctx: MulDivContext): Int =
    val left = visit(ctx.expr(0))
    val right = visit(ctx.expr(1))
    if ctx.op.getType == MUL then left * right
    else left / right

  override def visitAddSub(ctx: AddSubContext): Int =
    val left = visit(ctx.expr(0))
    val right = visit(ctx.expr(1))
    if ctx.op.getType == ADD then left + right
    else left - right

  override def visitParens(ctx: ParensContext): Int =
    visit(ctx.expr())
