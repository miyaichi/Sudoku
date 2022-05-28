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
 4. hint <row> <col>
 5. set <row> <col> <value>
 6. undo
 7. quit
```

2. GUI swing mode

![](./SudokuScreen.png | width=100)

## Usage:

```sh
$ java Sudoku [repl | swing]
```

- repl: run in cui repl mode
- swing: run in gui swing mode

## Features:

Sudoku game logic with the new game, solve the game, provide a hint, undo an action, reset the game.

数独ゲームの、作成、解く、ヒント、アクションを元に戻す、ゲームをリセットを実装しました。

## To-Do:

- Game Creation - I have created a goal and then randomly blanked cells, but the blank positions are biased and have not created a good game.

- ゲーム作成 - ゴールを作ってからランダムにセルを空白にしましたが、空白の位置が偏ってしまい、良いゲームが作成できていません。

- Animation during solving - In the Solve function, I wanted to use backtracking to show the step-by-step process of finding the goal. However, I could not effectively implement the ActionListener function to display the animation.

- 解く時のアニメーション - Solve 関数では、バックトラックを使用してゴールを見つけるまでの過程を段階的に表示したいと考えました。しかし、アニメーションを表示するための ActionListener 関数を効果的に実装することができませんでした。
