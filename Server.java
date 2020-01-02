package tictactoe;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Server {
	public static void main(String[] args) throws IOException
	{
		//variables
		ServerSocket listeningSocket = new ServerSocket(2403);	
		Scanner userInput = new Scanner(System.in);
		char[] board = new char[9];
		int serverChoice;
		int clientChoice;
		boolean playAgain = false;
		boolean exitGame = false;
		
		//wait for client to connect
		System.out.println("Waiting for client to connect...");
		Socket clientSocket = listeningSocket.accept();
		Scanner getMessagesFromClient = new Scanner(clientSocket.getInputStream());
		PrintStream sendToClient = new PrintStream(clientSocket.getOutputStream());
		
		//tell player game has began
		System.out.println("The game has begun.");
		
		//initialize board
		initializeBoard(board);
		
		//game loop
		while(true)
		{
			//tell player that other player is making their move
			System.out.println("Waiting for opponent to make their move...");
			
			//wait for clients input
			clientChoice = getMessagesFromClient.nextInt();
			
			//client won
			if(clientChoice < 0)
			{
				//put clientChoice into board
				clientChoice = clientChoice * -1;
				board[clientChoice - 1] = 'c';
				
				//display board
				displayBoard(board);
				
				//tell player they lost
				System.out.println("You LOST!!");
				
				//local variables
				String temp;
				char serverResponse;
				
				//get players response
				while (true)
				{
					System.out.print("Do you want to play again? (y/n) ");
					temp = userInput.nextLine();
					temp = temp.toLowerCase();
					serverResponse = temp.charAt(0);
					
					if ((serverResponse != 'y' && serverResponse != 'n') || temp.length() > 1)
					{
						System.out.println("Error: You must enter 'y' or 'n'.");
						continue;
					}
					
					else
					{
						break;
					}
				}
				
				//get clients response
				System.out.println("Waiting for opponent to decide.");
				getMessagesFromClient.nextLine();
				temp = getMessagesFromClient.nextLine();
				char clientResponse = temp.charAt(0);
				
				//if the players are going to play again
				if (clientResponse == 'y' && serverResponse == 'y')
				{
					//tell client we're playing again
					sendToClient.println(100);
					
					//tell player we're playing a new game
					System.out.println("A new game has begun.");
					
					//reset board
					initializeBoard(board);
					
					//wait for client to make first move
					continue;
				}
				
				//players are done playing
				else
				{
					//tell client where done
					sendToClient.println(200);
					
					//good bye message
					System.out.println("Thank you for playing my game!");
					
					//exit while loop
					break;
				}
			}
			
			//there was a tie
			else if (clientChoice == 300)
			{
				//get what client picked last
				clientChoice = getMessagesFromClient.nextInt();
				getMessagesFromClient.nextLine();
				
				//add clients choice to board
				board[clientChoice -1] = 'c';
				
				//display board
				displayBoard(board);
				
				//tell the player there was a tie
				System.out.println("The game ended in a tie.");
				
				//get response from player
				while (true)
				{
					System.out.print("Do you want to play again? (y/n) ");
					String temp = userInput.nextLine();
					temp = temp.toLowerCase();
					
					char serverResponse = temp.charAt(0);

					if ((serverResponse == 'y' || serverResponse == 'n') && temp.length() == 1)
					{
						System.out.println("Waiting for opponent to decide.");
						temp = getMessagesFromClient.nextLine();
						char clientResponse = temp.charAt(0);
						
						//if both say yes
						if (serverResponse == 'y' && clientResponse == 'y')
						{
							//tell client we're playing again
							sendToClient.println(100);
							
							//tell player we're playing a new game
							System.out.println("A new game has begun.");
							
							//reset board
							initializeBoard(board);
							
							//go back to stop of game loop
							playAgain = true;
							break;
						}
						
						//otherwise
						else
						{
							//tell client where done
							sendToClient.println(200);
							
							//good bye message
							System.out.println("Thank you for playing my game!");
							
							//exit game loop
							exitGame = true;
							break;
						}
					}
					
					else
					{
						System.out.println("Your input is not valid. Please try again.");
						continue;
					}
				}
				
				//go back to top of game loop
				if (playAgain == true)
				{
					playAgain = false;
					continue;
				}
				
				//exit game loop
				else if (exitGame == true)
				{
					break;
				}
			}
			
			//game is still going
			else
			{
				//update board
				board[clientChoice - 1] = 'c';
			}
			
			//keep on looping until we get valid input
			while (true)
			{
				//display board for player
				displayBoard(board);
				
				//prompt user for a digit
				System.out.print("Enter a digit: ");
			
				//try to get digit from user
				try
				{
					serverChoice = userInput.nextInt();
					userInput.nextLine();
				}
			
				catch (InputMismatchException e)
				{
					System.out.println("Error: You did not enter a digit. Please try again.");
					continue;
				}
			
				//see if players choice is valid
				if(serverChoice < 1 || serverChoice > 9)
				{
					System.out.println("Error: Your input is invalid. Please enter a digit 1-9.");
					continue;
				}
			
				else if (board[serverChoice - 1] != 'n')
				{
					System.out.println("Error: That position is already taken. Please try again.");
					continue;
				}
				
				break;
			}
			
			//keep track of what players have entered
			board[serverChoice - 1] = 's';
			
			//if server has won
			if(victory(board))
			{
				//display board
				displayBoard(board);
				
				//tell player they won
				System.out.println("You WON!!");
				
				//tell client they lost
				sendToClient.println(-1 * serverChoice);
				
				//ask server if they want to play again
				while (true)
				{
					System.out.print("Do you want to play again? (y/n) ");
					String temp = userInput.nextLine();
					temp = temp.toLowerCase();
					
					char response = temp.charAt(0);
					
					if ((response == 'y' || response == 'n') && temp.length() == 1)
					{
						//send response to client
						sendToClient.println(response);
						
						//get response from client
						System.out.println("Waiting for opponent to decide.");
						int code = getMessagesFromClient.nextInt();
						
						//if were playing again
						if (code == 100)
						{
							//tell player we are playing again
							System.out.println("A new game has begun.");
							
							//reset board
							initializeBoard(board);
							
							//set boolean to true
							playAgain = true;
							
							//break out of inner loop
							break;
						}
						
						else if (code == 200)
						{
							//good bye message
							System.out.println("Thank you for playing my game!");
							
							//prepare to exit game
							exitGame = true;
							
							break;
						}
					}
					
					else
					{
						System.out.println("Your input is not valid. Please try again.");
						continue;
					}
				}
				
				//go back to top of game loop
				if (playAgain == true)
				{
					playAgain = false;
					continue;
				}
				
				//exit game loop
				else if (exitGame == true)
				{
					break;
				}
			}
			
			//display board for client
			displayBoard(board);
			
			//send move to client
			sendToClient.println(serverChoice);
		}
		
		//close sockets
		clientSocket.close();
		listeningSocket.close();
		
		//free up resources
		userInput.close();
		getMessagesFromClient.close();
	}
	
	private static void displayBoard(char[] board)
	{
		System.out.println();
		
		for (int i = 0; i < 9; ++i)
		{
			if (board[i] == 'n')
			{
				System.out.print(i + 1);
			}
			
			else if (board[i] == 'c')
			{
				System.out.print('X');
			}
			
			else if (board[i] == 's')
			{
				System.out.print('O');
			}
			
			if ((i + 1) % 3 == 0)
			{
				System.out.println();
			}
		}
		
		System.out.println();
	}
	
	private static boolean victory(char[] board)
	{
		/*
		 * OOO
		 * 456
		 * 789
		 */
		if (board[0] == 's' && board[1] == 's' && board[2] == 's')
		{
			return true;
		}
		
		/*
		 * 123
		 * OOO
		 * 789
		 */
		if (board[3] == 's' && board[4] == 's' && board[5] == 's')
		{
			return true;
		}
		
		/*
		 * 123
		 * 456
		 * OOO
		 */
		if (board[6] == 's' && board[7] == 's' && board[8] == 's')
		{
			return true;
		}
		
		/*
		 * O23
		 * O56
		 * O89
		 */
		if (board[0] == 's' && board[3] == 's' && board[6] == 's')
		{
			return true;
		}
		
		/*
		 * 1O3
		 * 4O6
		 * 7O9
		 */
		if (board[1] == 's' && board[4] == 's' && board[7] == 's')
		{
			return true;
		}
		
		/*
		 * 12O
		 * 45O
		 * 78O
		 */
		if (board[2] == 's' && board[5] == 's' && board[8] == 's')
		{
			return true;
		}
		
		/*
		 * O23
		 * 4O6
		 * 78O
		 */
		if (board[0] == 's' && board[4] == 's' && board[8] == 's')
		{
			return true;
		}
		
		/*
		 * 12O
		 * 4O6
		 * O89
		 */
		if (board[2] == 's' && board[4] == 's' && board[6] == 's')
		{
			return true;
		}
		
		//otherwise
		return false;
	}
	
	private static void initializeBoard(char[] board)
	{
		//initialize board
		for (int i = 0; i < 9; ++i)
		{
			board[i] = 'n';
		}
	}
}