# 3 Card Poker Server - Project 3

A JavaFX-based server application for networked 3 Card Poker game. This server handles up to 8 simultaneous clients, each playing independent games against the dealer.

## Requirements

- Java 11 or higher
- Maven 3.6.3 or higher

## Building the Project

```bash
mvn clean compile
```

## Running the Server

Start the server from the command line:

```bash
mvn clean javafx:run
```

The server UI will launch with an intro scene where you can:
1. Enter a port number (default: 5555)
2. Click "Start Server" to begin accepting client connections

Once started, the server switches to a status view showing:
- Number of connected clients
- Game results for each client
- Betting information (ante, pair plus, play bets)
- Win/loss amounts per game
- Client connection/disconnection events

## Client Connection

Clients should connect to:
- **Host**: `localhost`
- **Port**: The port number you specified when starting the server (default: 5555)

## Game Rules

### Betting
- **Ante Bet**: Required, between 5 and 25
- **Pair Plus Bet**: Optional, between 5 and 25 (if placed)

### Game Flow
1. Client places Ante bet (and optionally Pair Plus bet)
2. Server deals 3 cards to player (face up) and 3 cards to dealer (face down)
3. Player decides to **Play** or **Fold**
   - **Fold**: Player loses Ante and Pair Plus bets
   - **Play**: Player must wager Play bet equal to Ante bet
4. If Play:
   - Dealer qualifies if hand is Queen high or better
   - If dealer not qualified: Play bet returned, Ante pushes
   - If dealer qualified: Compare hands
     - Dealer wins: Player loses Ante + Play
     - Player wins: Paid 1:1 on Ante and Play (receives double each wager)
     - Tie: Push
5. Pair Plus evaluation (if player didn't fold):
   - If hand < Pair of 2s: Lose Pair Plus bet
   - Else payout:
     - Straight Flush: 40:1
     - Three of a Kind: 30:1
     - Straight: 6:1
     - Flush: 3:1
     - Pair: 1:1

### Hand Rankings (high to low)
1. Straight Flush
2. Three of a Kind
3. Straight
4. Flush
5. Pair
6. High Card

## Communication Protocol

All client-server communication uses `PokerInfo` objects (implements `Serializable`). Message types:
- `INITIAL_BET`: Client sends initial bets
- `CARDS_DEALT`: Server sends dealt cards
- `PLAYER_ACTION`: Client sends PLAY or FOLD decision
- `GAME_RESULT`: Server sends final game result
- `PLAY_AGAIN`: Client requests another hand
- `DISCONNECT`: Client disconnects

## Architecture

### Key Classes
- **ServerApp**: Main JavaFX Application
- **IntroController**: Handles port input and server startup
- **StatusController**: Manages server status display and logging
- **PokerServer**: Manages ServerSocket and client connections (runs on background thread)
- **ClientHandler**: Handles individual client game logic (each on own thread)
- **ThreeCardLogic**: Static utility for hand evaluation and comparison
- **Deck**: 52-card deck with shuffle functionality
- **Card, Suit, Rank**: Card representation
- **PokerInfo**: Serializable communication object

### Threading
- Server runs on background thread (not JavaFX thread)
- Each client handled on its own thread
- UI updates use `Platform.runLater()` for thread safety

## Testing

Run JUnit 5 tests:

```bash
mvn test
```

Tests cover:
- Hand evaluation (all hand types)
- Hand comparison (win/lose/tie cases)
- Pair Plus payout calculations
- Dealer qualification logic

## Stopping the Server

Click the "Stop Server" button in the status view to gracefully shut down:
- Closes all client connections
- Stops accepting new connections
- Closes ServerSocket
- Logs shutdown events

## Notes

- Maximum 8 clients simultaneously
- Each client has independent game state (own deck, winnings tracking)
- All game computations performed on server
- Client is display and input only
- Graceful error handling and exception management
- Proper resource cleanup (sockets, streams)

