package dev.bjb.htx

import dev.bjb.htx.grammar.TemplateLexer
import dev.bjb.htx.grammar.TemplateParser
import dev.bjb.htx.grammar.TemplateBaseVisitor
import org.antlr.v4.runtime.*
import org.antlr.v4.runtime.tree.*

def getParser[L <: Lexer, P <: Parser](
    lexer: CharStream => L,
    parser: CommonTokenStream => P,
    contents: String,
): P = parser(CommonTokenStream(lexer(CharStreams.fromString(contents))))

class TemplateParserTest extends CommonSuite:
  test("ok".only) {
    val visitor = TemplateVisitor()
    val parser = getParser(
      TemplateLexer(_),
      TemplateParser(_),
      "1+1\n",
    )
    val tree = parser.prog()

    val result = visitor.visit(tree)
    assertEquals(result, 2) //
  }

class TemplateVisitor extends TemplateBaseVisitor[Int]:
  import TemplateParser.*

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
