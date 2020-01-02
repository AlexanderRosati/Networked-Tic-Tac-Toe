package tictactoe;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Client 
{	
	public static void main(String[] args) throws IOException
	{	
		//get IP address
		Scanner userInput = new Scanner(System.in);
		System.out.print("Enter the IP Address of your opponent: ");
		String ipAddress = userInput.nextLine();
		
		//variables
		Socket socket = new Socket(ipAddress, 2403);
		Scanner serverMessages = new Scanner(socket.getInputStream());
		PrintStream toServer = new PrintStream(socket.getOutputStream());
		char[] board = new char[9];
		int clientChoice;
		int serverChoice;
		boolean playAgain = false;
		boolean exitGame = false;
		
		//tell player game has began
		System.out.println("The game has begun.");
		
		//initialize board
		initializeBoard(board);
		
		//game loop
		while (true)
		{
			//display board
			displayBoard(board);
			
			//prompt user for a digit
			System.out.print("Enter a digit: ");
			
			//try to get digit from user
			try
			{
				clientChoice = userInput.nextInt();
				userInput.nextLine();
			}
			
			catch (InputMismatchException e)
			{
				System.out.println("Error: You did not enter a digit. Please try again.");
				continue;
			}
			
			//see if move is valid
			if (clientChoice < 1 || clientChoice > 9)
			{
				System.out.println("Error: Your input is invalid. Please enter a digit 1-9.");
				continue;
			}
			
			else if (board[clientChoice - 1] != 'n')
			{
				System.out.println("Error: That position is already taken. Please try again.");
				continue;
			}
			
			//keep track of what players have entered
			board[clientChoice - 1] = 'c';
			
			//if the client has won
			if (victory(board))
			{
				//display board
				displayBoard(board);
				
				//tell player they won
				System.out.println("You WON!!");
				
				//tell server they lost
				toServer.println(-1 * clientChoice);
				
				//ask client is they want to play again
				while (true)
				{
					System.out.print("Do you want to play again? (y/n) ");
					String temp = userInput.nextLine();
					temp = temp.toLowerCase();
					
					char response = temp.charAt(0);
					
					if ((response == 'y' || response == 'n') && temp.length() == 1)
					{
						//send response to server
						toServer.println(response);
						
						//get response from server
						System.out.println("Waiting for opponent to decide.");
						int code = serverMessages.nextInt();
						
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
						
						//ending the session
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
			}
			
			//if the two players tied
			else if (tie(board))
			{
				//display board
				displayBoard(board);
				
				//tell the player there was a tie
				System.out.println("The game ended in a tie.");
				
				//tell server there was a tie
				toServer.println(300);
				
				//send server clients choice
				toServer.println(clientChoice);
				
				//get response from user
				while (true)
				{
					System.out.print("Do you want to play again? (y/n) ");
					String temp = userInput.nextLine();
					temp = temp.toLowerCase();
					
					char response = temp.charAt(0);
					
					if ((response == 'y' || response == 'n') && temp.length() == 1)
					{
						//send response to server
						toServer.println(response);
						
						//get response from server
						System.out.println("Waiting for opponent to decide.");
						int code = serverMessages.nextInt();
						
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
						
						//ending the session
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
			
			//display board for client
			displayBoard(board);
			
			//send clients choice to server
			toServer.println(clientChoice);
			
			//tell player that other player is making their move
			System.out.println("Waiting for opponent to make their move...");
			
			//receive other players move
			serverChoice = serverMessages.nextInt();
			
			//server won
			if (serverChoice < 0)
			{
				//put servers choice into board
				serverChoice = -1 * serverChoice;
				board[serverChoice - 1] = 's';
				
				//display board
				displayBoard(board);
				
				//tell player they lost
				System.out.println("You LOST!!");
				
				//local variables
				String temp;
				char clientResponse;
				
				while (true)
				{
					System.out.print("Do you want to play again? (y/n) ");
					temp = userInput.nextLine();
					temp = temp.toLowerCase();
					clientResponse = temp.charAt(0);
					
					if ((clientResponse != 'y' && clientResponse != 'n') || temp.length() > 1)
					{
						System.out.println("Error: You must enter 'y' or 'n'.");
						continue;
					}
					
					else
					{
						break;
					}
				}
				
				//get server response
				System.out.println("Waiting for opponent to decide.");
				serverMessages.nextLine();
				temp = serverMessages.nextLine();
				char serverResponse = temp.charAt(0);
				
				//if the players are going to play again
				if (clientResponse == 'y' && serverResponse == 'y')
				{
					//tell client we're playing again
					toServer.println(100);
					
					//tell player we're playing a new game
					System.out.println("A new game has begun.");
					
					//reset board
					initializeBoard(board);
					
					//go back to top of game loop
					continue;
				}
				
				//players are done playing
				else
				{
					//tell server where done
					toServer.println(200);
					
					//good bye message
					System.out.println("Thank you for playing my game!");
					
					//exit while loop
					break;
				}
			}
			
			//game is still going
			else
			{
				//update board
				board[serverChoice - 1] = 's';
			}
		}
		
		//close socket
		socket.close();
		
		//free up resources
		userInput.close();
		serverMessages.close();
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
		 * XXX
		 * 456
		 * 789
		 */
		if (board[0] == 'c' && board[1] == 'c' && board[2] == 'c')
		{
			return true;
		}
		
		/*
		 * 123
		 * XXX
		 * 789
		 */
		if (board[3] == 'c' && board[4] == 'c' && board[5] == 'c')
		{
			return true;
		}
		
		/*
		 * 123
		 * 456
		 * XXX
		 */
		if (board[6] == 'c' && board[7] == 'c' && board[8] == 'c')
		{
			return true;
		}
		
		/*
		 * X23
		 * X56
		 * X89
		 */
		if (board[0] == 'c' && board[3] == 'c' && board[6] == 'c')
		{
			return true;
		}
		
		/*
		 * 1X3
		 * 4X6
		 * 7X9
		 */
		if (board[1] == 'c' && board[4] == 'c' && board[7] == 'c')
		{
			return true;
		}
		
		/*
		 * 12X
		 * 45X
		 * 78X
		 */
		if (board[2] == 'c' && board[5] == 'c' && board[8] == 'c')
		{
			return true;
		}
		
		/*
		 * X23
		 * 4X6
		 * 78X
		 */
		if (board[0] == 'c' && board[4] == 'c' && board[8] == 'c')
		{
			return true;
		}
		
		/*
		 * 12X
		 * 4X6
		 * X89
		 */
		if (board[2] == 'c' && board[4] == 'c' && board[6] == 'c')
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
	
	private static boolean tie(char[] board)
	{
		for (int i = 0; i < 9; ++i)
		{
			if (board[i] == 'n')
			{
				return false;
			}
		}
		
		return true;
	}
}