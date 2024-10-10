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

## Chapter 9

### Something interesting

1. Lox中的for通过desugaring这种有趣的想法实现，用户编写for循环语句，但是在解释器看来，这其实是个while循环，后端脱去了给用户提供的for语法糖

### Challenges

1. 在接下来的几章中，当Lox支持一级函数和动态调度时，从技术上讲，我们就不需要在语言中内置分支语句。说明如何用这些特性来实现条件执行。说出一种在控制流中使用这种技术的语言。

   Scheme和JavaScript能使用这些特性实现条件执行

   例如在JavaScript中，可以通过函数作为参数传递来实现条件执行(传入的不同函数实现不同的逻辑)

   注1：一级函数，即函数是"一等公民"，具备这一特性的语言通常称为支持函数式编程

   注2：动态调度，其实就是在运行时根据对象的实际类型来决定调用哪个方法的技术(Java中的动态单分派？)

2. 同样地，只要我们的解释器支持一个重要的优化，循环也可以用这些工具来实现。它是什么？为什么它是必要的？请说出一种使用这种技术进行迭代的语言。

   递归？ Scheme语言通过递归实现循环   (查了下发现应该说是尾递归优化)
3. support for "break;": 使用isBroken和isInBlock完成break语句，实现的break语句只能在块中使用，会跳转直到遇到while
