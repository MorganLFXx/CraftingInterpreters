# Note

## Chapter 6

### Challenges

1. "," operator: 添加了comma expression生成式，可以生成多个表达式的序列 commaExpression -> expression ( "," expression )*
2. "?" operator: 添加了three way生成式

   expression -> threeWay

   threeWay -> equality ( "?" threeWay ":" threeWay )?
3. 检查缺少操作数的二元操作符

## Chapter 7

### Challenges

1. Mixed type comparison: 我认为混合类型的比较过于混乱，暂未考虑采取实现
2. "+" operator: 完成了字符串和数字拼接的实现，这并不困难
3. /0 problem: 我使用了/0会报错的实现，但考虑到Lox中的值均为浮点数，我认为/0返回无穷大的实现也是合理的。