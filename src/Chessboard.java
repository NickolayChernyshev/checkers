import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.ArrayList;


/**
 * Данная панель позволяет сыграть двоим партию в шашки друг против друга. Красный всегда начинает игру. Если игрок может перейти перепрыгнуть шашку другого («съесть» ее), то он обязан это сделать. Если игрок не может сделать ни одного хода, то игра заканчивается.
 * В классе имеется  основной метод main(), который позволяет запустить его как автономное приложение. Приложение открывает окно, которое использует объект типа Checkers, как его панель с содержимым.
 * Существует также вложенный класс, Checker.Applet, который может быть использован в качестве небольшой версии программы («апплета»). Размеры апплета должны составлять 600 на 440 (или очень близко к этому).
 */
public class Chessboard extends JPanel {

    /**
     * Метод main позволяет запустить шашки как автономное приложение. Он открывает окно, которое показывает доску с шашками. Игра заканчивается, когда пользователь закрывает это окно.
     */
    public static void main(String[] args) {
        JFrame window = new JFrame("Checkers");
        Chessboard content = new Chessboard();
        window.setContentPane(content);
        window.pack();
        Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
        window.setLocation( (screensize.width - window.getWidth())/2,
                (screensize.height - window.getHeight())/2 );
        window.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        window.setResizable(false);
        window.setVisible(true);
    }

    private JButton newGameButton;  // Кнопка для начала новой игры
    private JButton resignButton;   // Кнопка, на которую может нажать игрок для того, чтобы «сдаться» (победит второй игрок).

    private JLabel message;  // Объект, который позволяет показывать сообщения игрокам.

    /**
     * Конструктор создает доску для игры (который, в свою очередь, создает и управляет кнопками и объектами с сообщениями), добавляет все составляющие и устанавливает границы компонентов.
     */
    public Chessboard() {

        setLayout(null);  // Не будет использоваться никакой заранее созданный макет.
        setPreferredSize( new Dimension(600,440) ); // Размеры открывающегося окна – 600 на 440.

        setBackground(new Color(0,77,255));  // Подложка в данном случае будет синего цвета.

      /* Создаем составляющие и добавляем их в апплет */

        Board board = new Board();  // Примечание: Конструктор для доски создает также кнопки и метки.
        add(board);
        add(newGameButton);
        add(resignButton);
        add(message);

/* Установим позиции и размеры каждого компонента при помощи вызова для каждого собственного метода setBounds(). */
        board.setBounds(20,20,325,325); // Примечание:  размер окошечка с доской равен 325 на 325.
        newGameButton.setBounds(390, 60, 180, 30);
        resignButton.setBounds(390, 120, 180, 30);
        message.setBounds(0, 200, 600, 420);

    } // Завершение конструктора



// --------------------  Вложенные классы -------------------------------
    /**
     * Объект CheckersMove представляет собой ход в игре шашки. Он содержит данные о клетке, в которой изначально находится шашка (по горизонтали и по вертикали), которой будет осуществлен ход, и клетка, в которую придет шашка после хода. (Этот класс не гарантирует того, что такой ход возможен).
     */
    private static class CheckersMove {
        int fromRow, fromCol;  // Клетка с шашкой, которой игрок «будет ходить».
        int toRow, toCol;      // Клетка, в которую переместится шашка.
        CheckersMove(int r1, int c1, int r2, int c2) {
            // Конструктор, который устанавливает переменные значения (положение шашки в клетках в ходе движения по горизонтали и вертикали)
            fromRow = r1;
            fromCol = c1;
            toRow = r2;
            toCol = c2;
        }
        boolean isJump() {
// Проверка, можно ли «съесть» шашку этим ходом. Предполагается, что ход является возможным. Во время такого хода шашка перемещается на две клетки (во время обычного хода она перемещается только на одну клетку)
            return (fromRow - toRow == 2 || fromRow - toRow == -2);
        }
    }  // Завершение класса CheckersMove.
    /**
     * Эта панель отображает доску для игры в шашки размером 325 на 325 с двухпиксельной черной рамкой. Этот класс выполняет работу, позволяющую пользователям играть в шашки и отображает доску.
     */
    private class Board extends JPanel implements ActionListener, MouseListener {


        CheckersData board; // Данные для доски с шашками хранятся здесь.
        //    Эта доска также отвечают за составление списка возможных ходов.

        boolean gameInProgress; // Идет ли уже игра?

      /* Следующие три переменные действительны только в том случае, когда игра уже идет */

        int currentPlayer;      // Чей сейчас ход? Возможными значениями являются  CheckersData.RED и CheckersData.BLACK.
        int selectedRow, selectedCol;  // Если текущий игрок выбрал шашку, чтобы выполнить ход, они дают положение клетки, с которой находится эта шашка. Если же шашка еще не выбрана, то клетка равна -1.

        CheckersMove[] legalMoves;  // Массив, который содержит возможные ходы для текущего игрока.

        /**
         * Конструктор. Создает кнопки и метки. «Прислушивается» к кликам мыши в игре и кликам по кнопкам. Создает доску и начинает первую игру.
         */
        Board() {
            setBackground(Color.BLACK);
            addMouseListener(this);
            resignButton = new JButton("Сдаться");
            resignButton.addActionListener(this);
            newGameButton = new JButton("Новая игра");
            newGameButton.addActionListener(this);
            message = new JLabel("",JLabel.CENTER);
            message.setFont(new  Font("Serif", Font.BOLD, 20));
            message.setForeground(Color.yellow);
            board = new CheckersData();
            doNewGame();
        }


        /**
         * Ответная реакция на клик пользователя по одной из двух кнопок.
         */
        public void actionPerformed(ActionEvent evt) {
            Object src = evt.getSource();
            if (src == newGameButton)
                doNewGame();
            else if (src == resignButton)
                doResign();
        }


        /**
         * Начало новой игры
         */
        void doNewGame() {
            if (gameInProgress == true) {
                // Это должно быть так, но не помешает проверить
                message.setText("Сначала завершите текущую игру!");
                return;
            }
            board.setUpGame();   // Готовим шашки к игре.
            currentPlayer = CheckersData.RED;   // Красный начинает первым.

            legalMoves = board.getLegalMoves(CheckersData.RED);  // Показывает возможные ходы для пользователя, играющего красными.
            selectedRow = -1;   // Красные не выбрали шашку, которой будут ходить.
            message.setText("Красные, сделайте свой ход");
            gameInProgress = true;
            newGameButton.setEnabled(false);
            resignButton.setEnabled(true);
            repaint();
        }


        /**
         * Текущий игрок сдается. Игра заканчивается. Соперник выигрывает
         */
        void doResign() {
            if (gameInProgress == false) {  // В принципе, такого быть не может.
                message.setText("Не идет ни одна игра!");
                return;
            }
            if (currentPlayer == CheckersData.RED)
                gameOver("Красные сдаются, черные выигрывают.");
            else
                gameOver("Черные сдаются, красные выигрывают.");
        }


        /**
         * Игра заканчивается. Параметр str отображается как сообщение для пользователя.
         Места шашек приведены в начальное положение, что означает, что игроки могут начать новую игру. Этот метод вызывается, когда игра заканчивается, в любой момент этого класса.
         */
        void gameOver(String str) {
            message.setText(str);
            newGameButton.setEnabled(true);
            resignButton.setEnabled(false);
            gameInProgress = false;
        }


        /**
         * Данный тип вызывается при помощи mousePressed(), когда игрок кликает на определенную клетку по горизонтали и вертикали. К этому моменту, уже достоверным является тот факт, что игра находится в процессе.
         */

        void doClickSquare(int row, int col) {

         /* Если игрок кликнул на одну из своих шашек, которой он может сделать ход, то отмечаем шашку, на которую нажали мышкой, и возвращаемся с помощью оператора return.  (Это может изменить предыдущий выбор.)  Удаляем сообщение в том случае, если ранее было выведено сообщение об ошибке.
*/

            for (int i = 0; i < legalMoves.length; i++)
                if (legalMoves[i].fromRow == row && legalMoves[i].fromCol == col) {
                    selectedRow = row;
                    selectedCol = col;
                    if (currentPlayer == CheckersData.RED)
                        message.setText("Красные, сделайте свой ход.");
                    else
                        message.setText("Черные, сделайте свой ход.");
                    repaint();
                    return;
                }

         /* В том случае, если не была выбрана шашка для хода, пользователь обязан сначала выбрать шашку. Необходимо вывести сообщение об ошибке и вернуться назад. */

            if (selectedRow < 0) {
                message.setText("Кликните на шашку для хода.");
                return;
            }

/*В случае, если пользователь кликнул на клетку, куда может быть осуществлен ход выбранной шашкой, делаем ход и возвращаемся.
 */

            for (int i = 0; i < legalMoves.length; i++)
                if (legalMoves[i].fromRow == selectedRow && legalMoves[i].fromCol == selectedCol
                        && legalMoves[i].toRow == row && legalMoves[i].toCol == col) {
                    doMakeMove(legalMoves[i]);
                    return;
                }

                  /* Если получается так, что шашка выбрана, а клетка, в которую пользователь хочет сделать ход, недоступна (такого хода нет), выводим сообщение об ошибке.
*/


            message.setText("Нажмите на клетку, куда будет сделан ход.");

        }  // Завершение doClickSquare()


        /**
         * Данный тип вызывается, когда текущий игрок выбрал ход. Осуществляем ход и далее либо заканчиваем, либо продолжаем игру соответственно.
         */
        void doMakeMove(CheckersMove move) {

            board.makeMove(move);

/* Если во время хода была «съедена» шашка соперника, то возможно, что игрок может «съесть» и еще одну шашку противника. Необходимо проверить наличие возможных ходов, начиная с той клетки, в которую только что был сделан ход. Если есть возможность сделать ход, игрок должен «съесть» еще одну шашку. Тот же самый игрок продолжает ход.
          */

            if (move.isJump()) {
                legalMoves = board.getLegalJumpsFrom(currentPlayer,move.toRow,move.toCol);
                if (legalMoves != null) {
                    if (currentPlayer == CheckersData.RED)
                        message.setText("Красные, вы должны съесть еще шашку.");
                    else
                        message.setText("Черные, вы должны съесть еще шашку.");
                    selectedRow = move.toRow;  // Если можно сделать ход только одной шашкой, выбираем ее.
                    selectedCol = move.toCol;
                    repaint();
                    return;
                }
            }

         /* Ход текущего игрока завершен, поэтому ход переходит к другому игроку. Проверяем его возможные ходы. В том случае, если таковых нет, игра заканчивается.
*/

            if (currentPlayer == CheckersData.RED) {
                currentPlayer = CheckersData.BLACK;
                legalMoves = board.getLegalMoves(currentPlayer);
                if (legalMoves == null)
                    gameOver("У Черных нет возможных ходов, Красные выиграли.");
                else if (legalMoves[0].isJump())
                    message.setText("Черные, сделайте свой ход. Вы должны съесть шашку.");
                else
                    message.setText("Черные, сделайте свой ход.");
            }
            else {
                currentPlayer = CheckersData.RED;
                legalMoves = board.getLegalMoves(currentPlayer);
                if (legalMoves == null)
                    gameOver("У Красных нет возможных ходов, Черные выиграли.");
                else if (legalMoves[0].isJump())
                    message.setText("Красные, сделайте свой ход. Вы должны съесть шашку.");
                else
                    message.setText("Красные, сделайте свой ход.");
            }

         /* Устанавливаем selectedRow = -1 для того, чтобы отметить, что игрок еще не выбрал шашку для хода.
*/

            selectedRow = -1;

         /* В том случае, если все возможные ходы ограничены одной шашкой,  она выбирается автоматически.*/

            if (legalMoves != null) {
                boolean sameStartSquare = true;
                for (int i = 1; i < legalMoves.length; i++)
                    if (legalMoves[i].fromRow != legalMoves[0].fromRow
                            || legalMoves[i].fromCol != legalMoves[0].fromCol) {
                        sameStartSquare = false;
                        break;
                    }
                if (sameStartSquare) {
                    selectedRow = legalMoves[0].fromRow;
                    selectedCol = legalMoves[0].fromCol;
                }
            }

         /* Убеждаемся, что доска поменяла свое состояние. */

            repaint();

        }  // Завершение doMakeMove();


        /**
         * Рисуем доску для шашек. Используем серый и светло-серый цвета для клеток. Рисуем сами шашки. Если игра идет, подсвечиваем возможные ходы.
         */
        public void paintComponent(Graphics g) {

         /* Нарисуем двухпиксельную черную рамку по краям. */

            g.setColor(Color.black);
            g.drawRect(0,0,getSize().width-1,getSize().height-1);
            g.drawRect(1,1,getSize().width-3,getSize().height-3);

                  /* Рисуем клетки доски для шашек и сами шашки.
 */

            for (int row = 0; row < 8; row++) {
                for (int col = 0; col < 8; col++) {
                    if ( row % 2 == col % 2 )
                        g.setColor(Color.LIGHT_GRAY);
                    else
                        g.setColor(Color.GRAY);
                    g.fillRect(2 + col*40, 2 + row*40, 41, 41);
                    switch (board.pieceAt(row,col)) {
                        case CheckersData.RED:
                            g.setColor(Color.RED);
                            g.fillOval(6 + col*40, 8 + row*40, 30, 30);
                            break;
                        case CheckersData.BLACK:
                            g.setColor(Color.BLACK);
                            g.fillOval(6 + col*40, 8 + row*40, 30, 30);
                            break;
                        case CheckersData.RED_KING:
                            g.setColor(Color.RED);
                            g.fillOval(6 + col*40, 8 + row*40, 30, 30);
                            g.setColor(Color.WHITE);
                            g.fillOval(16 + col*40, 18 + row*40, 10, 10);
                            break;
                        case CheckersData.BLACK_KING:
                            g.setColor(Color.BLACK);
                            g.fillOval(6 + col*40, 8 + row*40, 30, 30);
                            g.setColor(Color.WHITE);
                            g.fillOval(16 + col*40, 18 + row*40, 10, 10);
                            break;
                    }
                }
            }

         /* Если игра идет, выделяем цветом возможные ходы. Примечание: legalMoves никогда не может принимать значение null в то время как идет игра. */

            if (gameInProgress) {
               /* Сначала рисуем четырехпиксельную цвета cyan вокруг шашек, которыми можно ходить. */
                g.setColor(Color.cyan);
                for (int i = 0; i < legalMoves.length; i++) {
                    g.drawRect(3 + legalMoves[i].fromCol*40, 3 + legalMoves[i].fromRow*40, 38, 38);
                    g.drawRect(5 + legalMoves[i].fromCol*40, 5 + legalMoves[i].fromRow*40, 34, 34);
                }
               /* Если шашка для хода выбрана (т.е. selectedRow >= 0), то рисуем четырехпискельную белую границу вокруг этой шашки и рисуем зеленые границы вокруг каждой клетки, в которую может переместиться эта шашка.
*/
                if (selectedRow >= 0) {
                    g.setColor(Color.white);
                    g.drawRect(3 + selectedCol*40, 3 + selectedRow*40, 38, 38);
                    g.drawRect(5 + selectedCol*40, 5 + selectedRow*40, 34, 34);
                    g.setColor(Color.green);
                    for (int i = 0; i < legalMoves.length; i++) {
                        if (legalMoves[i].fromCol == selectedCol && legalMoves[i].fromRow == selectedRow) {
                            g.drawRect(3 + legalMoves[i].toCol*40, 3 + legalMoves[i].toRow*40, 38, 38);
                            g.drawRect(5 + legalMoves[i].toCol*40, 5 + legalMoves[i].toRow*40, 34, 34);
                        }
                    }
                }
            }

        }  // Завершение paintComponent()


        /**
         * Реакция на клик пользователя по доске. Если не идет ни одна игра, выводится сообщения об ошибке. В противном случае, находим положение клетки по горизонтали и вертикали, на которую нажал пользователь, и вызываем doClickSquare() для выделения.
         */
        public void mousePressed(MouseEvent evt) {
            if (gameInProgress == false)
                message.setText("Нажмите \"Новая игра\" чтобы начать новую игру.");
            else {
                int col = (evt.getX() - 2) / 40;
                int row = (evt.getY() - 2) / 40;
                if (col >= 0 && col < 8 && row >= 0 && row < 8)
                    doClickSquare(row,col);
            }
        }


        public void mouseReleased(MouseEvent evt) { }
        public void mouseClicked(MouseEvent evt) { }
        public void mouseEntered(MouseEvent evt) { }
        public void mouseExited(MouseEvent evt) { }


    }  // Завершение класса Board
    /**
     * Объект этого класса хранит данные об игре. Он знает, какого цвета шашка находится на каждой из клеток доски. Красные идут вверх по доске (номер по вертикали увеличивается), а белые идут вниз (номер по горизонтали уменьшается). Методы служат для того, чтобы получать с помощью return списки возможных ходов
     */
    private static class CheckersData {

      /*  Следующие постоянные отображают возможные состояния клетки на доске. Постоянные RED и BLACK также отображают игроков в игре. */

        static final int
                EMPTY = 0,
                RED = 1,
                RED_KING = 2,
                BLACK = 3,
                BLACK_KING = 4;


        int[][] board;  // board[r][c] – количество клеток по горизонтали r и по вертикали c.


        /**
         * Конструктор, который создает доску и готовит ее к новой игре.
         */
        CheckersData() {
            board = new int[8][8];
            setUpGame();
        }


        /**
         * Этот тип устанавливает доску с шашками в позицию готовности к началу игры. Шашки могут быть только в тех клетках, которые соответствуют условию row % 2 == col % 2.  В начале игры  все такие клетки в первых трех рядах содержат черные шашки, а такие же клетки в последних трех рядах – красные шашки.
         */
        void setUpGame() {
            for (int row = 0; row < 8; row++) {
                for (int col = 0; col < 8; col++) {
                    if ( row % 2 == col % 2 ) {
                        if (row < 3)
                            board[row][col] = BLACK;
                        else if (row > 4)
                            board[row][col] = RED;
                        else
                            board[row][col] = EMPTY;
                    }
                    else {
                        board[row][col] = EMPTY;
                    }
                }
            }
        }  // Завершение setUpGame()


        /**
         * Возвращает содержимое соответствующей клетки по горизонтали и вертикали
         */
        int pieceAt(int row, int col) {
            return board[row][col];
        }


        /**
         * Выполнение хода. Предполагается, что ход не является нулевым (шашка сдвинута с места) и ход принадлежит к числу возможных.
         */
        void makeMove(CheckersMove move) {
            makeMove(move.fromRow, move.fromCol, move.toRow, move.toCol);
        }


        /**
         * Выполнение хода из (fromRow,fromCol) в (toRow,toCol).  Предполагается, что этот ход можно сделать. Если во время хода съедена фишка соперника, то она удаляется с доски. Если шашка переместится в последний ряд на стороне соперника, то шашка становится дамкой.
         */
        void makeMove(int fromRow, int fromCol, int toRow, int toCol) {
            board[toRow][toCol] = board[fromRow][fromCol];
            board[fromRow][fromCol] = EMPTY;
            if (fromRow - toRow == 2 || fromRow - toRow == -2) {
// Во время хода съедается фишка. Удаляем съеденную фишку с поверхности доски.
                int jumpRow = (fromRow + toRow) / 2;  // Положение съеденной шашки по горизонтали.
                int jumpCol = (fromCol + toCol) / 2;  // Положение съеденной шашки по вертикали.
                board[jumpRow][jumpCol] = EMPTY;
            }
            if (toRow == 0 && board[toRow][toCol] == RED)
                board[toRow][toCol] = RED_KING;
            if (toRow == 7 && board[toRow][toCol] == BLACK)
                board[toRow][toCol] = BLACK_KING;
        }

        /**
         * Возвращается массив, содержащий все возможные CheckersMoves (ходы) для определенного игрока на данной доске. Если возможные ходы отсутствуют, возвращается значение null. Обозначение игрока должно быть одной из постоянных RED или BLACK; в противном случае, возвращается null.  Если возвращается ненулевое значение, оно полностью состоит из ходов, которыми можно съесть шашку соперника, либо же полностью из обычных ходов. В том случае, если игрок может съесть шашку соперника, возможными являются только такие ходы.
         */
        CheckersMove[] getLegalMoves(int player) {

            if (player != RED && player != BLACK)
                return null;

            int playerKing;  // Постоянная отображает дамку, принадлежащую игроку.
            if (player == RED)
                playerKing = RED_KING;
            else
                playerKing = BLACK_KING;

            ArrayList<CheckersMove> moves = new ArrayList<CheckersMove>();  // Ходы будут храниться в этом списке.

         /*  Сначала проверяем на наличие возможных ходов. Смотрим на каждую клетку доски. Если клетка содержит одну из шашек игрока, ищем возможность съесть фишку соперника в каждом из четырех направлений от этой клетки. Если есть возможность такого хода, то помещаем его в массив ходов ArrayList.
          */

            for (int row = 0; row < 8; row++) {
                for (int col = 0; col < 8; col++) {
                    if (board[row][col] == player || board[row][col] == playerKing) {
                        if (canJump(player, row, col, row+1, col+1, row+2, col+2))
                            moves.add(new CheckersMove(row, col, row+2, col+2));
                        if (canJump(player, row, col, row-1, col+1, row-2, col+2))
                            moves.add(new CheckersMove(row, col, row-2, col+2));
                        if (canJump(player, row, col, row+1, col-1, row+2, col-2))
                            moves.add(new CheckersMove(row, col, row+2, col-2));
                        if (canJump(player, row, col, row-1, col-1, row-2, col-2))
                            moves.add(new CheckersMove(row, col, row-2, col-2));
                    }
                }
            }

                  /*  Если был найден хотя бы один ход, с помощью которого можно съесть шашку соперника, то игрок обязан его осуществить, поэтому мы не добавляем никаких обычных ходов. Однако если такие ходы не были найдены, ищем возможные ходы среди обычных. Смотрим на каждую клетку доски. Если клетка содержит одну из шашек игрока, ищем возможный ход в каждом из четырех направлений  от клетки. Если есть возможный ход в одном из направлений, то помещаем его в массив ходов ArrayList.
          */

            if (moves.size() == 0) {
                for (int row = 0; row < 8; row++) {
                    for (int col = 0; col < 8; col++) {
                        if (board[row][col] == player || board[row][col] == playerKing) {
                            if (canMove(player,row,col,row+1,col+1))
                                moves.add(new CheckersMove(row,col,row+1,col+1));
                            if (canMove(player,row,col,row-1,col+1))
                                moves.add(new CheckersMove(row,col,row-1,col+1));
                            if (canMove(player,row,col,row+1,col-1))
                                moves.add(new CheckersMove(row,col,row+1,col-1));
                            if (canMove(player,row,col,row-1,col-1))
                                moves.add(new CheckersMove(row,col,row-1,col-1));
                        }
                    }
                }
            }

         /* Если возможных ходов не было найдено, возвращаем null.  В противном случае, создаем массив, достаточно большой, чтобы хранить в нем все возможные ходы, копируем возможные ходы из массива ArrayList в данный массив и возвращаем массив. */

            if (moves.size() == 0)
                return null;
            else {
                CheckersMove[] moveArray = new CheckersMove[moves.size()];
                for (int i = 0; i < moves.size(); i++)
                    moveArray[i] = moves.get(i);
                return moveArray;
            }

        }  // Завершение getLegalMoves


        /**
         * Возвращает список всех возможных ходов, которые определенный игрок может начать делать из определенной клетки по горизонтали и вертикали. Если таких ходов, во время которых можно съесть шашку соперника, нет, то возвращается null. Такая же логика наблюдается в методе getLegalMoves().
         */
        CheckersMove[] getLegalJumpsFrom(int player, int row, int col) {
            if (player != RED && player != BLACK)
                return null;
            int playerKing;  // Постоянная отображает дамку, принадлежащую игроку.
            if (player == RED)
                playerKing = RED_KING;
            else
                playerKing = BLACK_KING;
            ArrayList<CheckersMove> moves = new ArrayList<CheckersMove>();  // Возможные ходы будут храниться в этом списке.
            if (board[row][col] == player || board[row][col] == playerKing) {
                if (canJump(player, row, col, row+1, col+1, row+2, col+2))
                    moves.add(new CheckersMove(row, col, row+2, col+2));
                if (canJump(player, row, col, row-1, col+1, row-2, col+2))
                    moves.add(new CheckersMove(row, col, row-2, col+2));
                if (canJump(player, row, col, row+1, col-1, row+2, col-2))
                    moves.add(new CheckersMove(row, col, row+2, col-2));
                if (canJump(player, row, col, row-1, col-1, row-2, col-2))
                    moves.add(new CheckersMove(row, col, row-2, col-2));
            }
            if (moves.size() == 0)
                return null;
            else {
                CheckersMove[] moveArray = new CheckersMove[moves.size()];
                for (int i = 0; i < moves.size(); i++)
                    moveArray[i] = moves.get(i);
                return moveArray;
            }
        }  // Завершение getLegalMovesFrom()


        /**
         * Проверяем, имеет ли право игрок съесть шашку соперника, переместившись из (r1,c1) в (r3,c3). Вызов осуществляется двумя предыдущими методами. Предполагается, что у игрока есть шашка в клетке (r1,c1), что (r3,c3) – положение клетки, которая находится на расстоянии двух клеток по горизонтали и вертикали от (r1,c1) и что (r2,c2) – клетка, которая находится между (r1,c1) и (r3,c3).
         */
        private boolean canJump(int player, int r1, int c1, int r2, int c2, int r3, int c3) {

            if (r3 < 0 || r3 >= 8 || c3 < 0 || c3 >= 8)
                return false;  // клетка (r3,c3) не входит в доску (находится за ее пределами).

            if (board[r3][c3] != EMPTY)
                return false; // клетка (r3,c3) уже содержит шашку.

            if (player == RED) {
                if (board[r1][c1] == RED && r3 > r1)
                    return false;  // Обычная красная шашка может двигаться только вверх по доске.
                if (board[r2][c2] != BLACK && board[r2][c2] != BLACK_KING)
                    return false;  // Нет ни одной черной шашки, которую можно съесть.
                return true;  // Ход, которым съедается шашка, возможен.
            }
            else {
                if (board[r1][c1] == BLACK && r3 < r1)
                    return false;  // Обычная черная шашка может двигаться только вниз по доске.
                if (board[r2][c2] != RED && board[r2][c2] != RED_KING)
                    return false;  // Нет ни одной красной шашки, которую можно съесть.
                return true;  // Ход, которым съедается шашка, возможен.
            }

        }  // Завершение canJump()


        /**
         * Вызывается методом getLegalMoves() чтобы определить, возможен ли ход игрока из (r1,c1) в (r2,c2). Предполагается, что (r1,r2) содержит одну из шашек игрока и (r2,c2) является соседней клеткой.
         */
        private boolean canMove(int player, int r1, int c1, int r2, int c2) {

            if (r2 < 0 || r2 >= 8 || c2 < 0 || c2 >= 8)
                return false;  // Клетка (r2,c2) не входит в доску.

            if (board[r2][c2] != EMPTY)
                return false;  // Клетка (r2,c2) уже содержит шашку.

            if (player == RED) {
                if (board[r1][c1] == RED && r2 > r1)
                    return false;  // Обычная красная шашка может двигаться только вверх по доске.
                return true;  // Ход возможен.
            }
            else {
                if (board[r1][c1] == BLACK && r2 < r1)
                    return false;  // Обычная черная шашка может двигаться только вверх по доске.
                return true;  // Ход возможен.
            }

        }  // Завершение canMove()


    } // Завершение класса CheckersData


} // Завершение класса Checkers
