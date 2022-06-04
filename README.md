# Sudoku

Simple implementation to learn Sudoku programming. Separate game and UI classes to check the game logic in the CUI and the game experience in the GUI.

数独プログラミングを学ぶためのシンプルな実装です。ゲームクラスと UI クラスを分離し、CUI でゲームロジックを、GUI でゲーム体験を確認できます。

1. CUI repl mode

```
% java Sudoku repl
-----+-----+-----+
. . 3 . 5 . . 8 9
. . . . 8 9 1 . .
. . . 1 . . . 5 .
. . 4 . . 5 . 9 7
. . . . . 8 . 1 .
. . 7 9 . 4 3 6 .
. . 1 5 . . 9 . .
. . 5 . . 7 2 . 1
9 . . . . . . 4 .
-----+-----+-----+
> set 0 0 1
Set 0 0 1 returns true
-----+-----+-----+
1 . 3 . 5 . . 8 9
. . . . 8 9 1 . .
. . . 1 . . . 5 .
. . 4 . . 5 . 9 7
. . . . . 8 . 1 .
. . 7 9 . 4 3 6 .
. . 1 5 . . 9 . .
. . 5 . . 7 2 . 1
9 . . . . . . 4 .
-----+-----+-----+
> help
Available commands:
 1. new
 2. reset
 3. solve
 4. hint
 5. set <row> <col> <value>
 6. undo
 7. quit
```

2. GUI swing mode

<img  src="https://user-images.githubusercontent.com/129797/172027354-910f97b1-6a1a-4c88-ad1c-cc4462826418.png" style="width: 50%"/>

## Usage:

```sh
$ java Sudoku [repl | swing]
```

- repl: run in cui repl mode
- swing: run in gui swing mode

## Features:

Sudoku game logic with the new game, solve the game, provide a hint, undo an action, reset the game.

数独ゲームの、作成、解く、ヒント、アクションを元に戻す、ゲームをリセットを実装しました。

## Solver

It implemented the following solver strategies.

以下のソルバー戦略を実装した。

- Nacked Single.

  If there is only one candidate, it is assumed to be the solution for that cell.

  候補が 1 つしかない場合、そのセルの解とする。

- Hidden Single reduction.

  If a candidate appears only once in a row, column, or block, it can reduce candidates.

  候補が行、列、ブロックに 1 回しか出現しない場合、候補を減らすことができる。

- Nacked Pairs, Triplets, Quads reduction.

  If the pairs, triples, and quads appear in the row, column, and block, it can reduce candidates.

  ペア、トリプル、クアッドが行、列、ブロックに出現していれば、候補を減らすことができる。

- Box/Line reduction.

  If a candidate only appears two or three times in a row or column and is all in the same block, it can reduce candidates.

  候補が行や列に 2 ～ 3 回しか登場せず、すべて同じブロックにある場合、候補を減らすことができる。

## To-Do:

- Animation during solving - In the Solve function, I wanted to use backtracking to show the step-by-step process of finding the goal. However, I could not effectively implement the ActionListener function to display the animation.

- 解く際のアニメーション - Solve 関数では、バックトラックを使用してゴールを見つけるまでの過程を段階的に表示したいと考えました。しかし、アニメーションを表示するための ActionListener 関数を効果的に実装することができませんでした。
