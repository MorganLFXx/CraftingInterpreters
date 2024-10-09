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

## Chapter 8

### Challenges

1. Display the result value: 通过在Lox增加了静态变量isInFile，在解释器中判断是否在文件中运行，从而决定是否输出结果。
2. Access uninitialized variables: 在除赋值和定义的地方检查Variable，封装了checkVarIsInitialized方法
3. 内层重名定义使用外层定义
   ```Lox
   var a = 1;
   {
      var a = a + 1;
   }
   ```
   Lox解释器不认为有误，Java解释器会报错未定义变量a