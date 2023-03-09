package com.everest.calculator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * @version 1.0.0
 * @author LiuFeng
 * @since 20230308
 */
public class Calculator {
	/**
	 * Note: the pre calculate result
	 */
	private BigDecimal preCalcuResult;
	/**
	 * Note: the current operator
	 */
	private String currOperator;
	/**
	 * Note: the current operand
	 */
	private BigDecimal currOperand;
	
	
	public void setCurrOperand(BigDecimal currOperand) {
		/**
		 * Note: initial preCalcuResult, if not happen any cal, the value is currOperand
		 */
		if (preCalcuResult == null) {
			preCalcuResult = currOperand;
		} else {
			this.currOperand = currOperand;
		}
	}

	public void setCurrOperator(String currOperator) {
		this.currOperator = currOperator;
	}

	/**
	 * the recent serial operand save collection
	 */
	private List<BigDecimal> recentSerialOperandList = new ArrayList<>();
	/**
	 * the recent serial operator save collection
	 */
	private List<String> recentSerialOperatorList = new ArrayList<>();
	/**
	 * the recent serial result save collection
	 */
	private List<BigDecimal> recentSerialResultList = new ArrayList<>();



	/**
	 * Note: the default precision is thress
	 */
	private int precision = 3;
	/**
	 * in the process of redo or undo, cover the undo or redo record, and record valid index max value
	 */
	private int recentOperatorIndex = -1;
	/**
	 * Note: undo or redo valid max index value
	 */
	private int maxValidIndex = -1;



	/**
	 * Note: the action of calculation
	 */
	public void execCalculator() {
		// Initiate calculator result value
		preCalcuResult = (preCalcuResult == null) ? BigDecimal.ZERO : preCalcuResult;
		if (currOperator == null) {
			System.out.println("Please select the operation~ ");
		}
		// Check the currOperand
		if (currOperand != null) { 
			/**
			 * Note: accumulate the calculate result
			 */
			BigDecimal interResult = calcTwoNum(currOperand, currOperator, preCalcuResult);
			/**
			 * -1 represent no any state, and not in the process of redo or undo, record relative key indicator
			 */
			if (this.recentOperatorIndex == -1) {
				recentSerialOperandList.add(currOperand);
				recentSerialOperatorList.add(currOperator);
				recentSerialResultList.add(preCalcuResult);
			} else {
				/**
				 * in the process of redo or undo, cover the undo or redo record, and record valid index max value
				 */
				this.recentOperatorIndex++;
				this.maxValidIndex = this.recentOperatorIndex;
				
				this.recentSerialResultList.set(this.recentOperatorIndex, interResult);
				this.recentSerialOperandList.set(this.recentOperatorIndex - 1, currOperand);
				this.recentSerialOperatorList.set(this.recentOperatorIndex - 1, currOperator);
			}
			preCalcuResult = interResult;
			currOperator = null;
			currOperand = null;
		}
	}
	
	/**
	 * Note: action accumulation calculate
	 * 
	 * @param preCalcuResult 
	 * @param currOperator   
	 * @param currOperand
	 */
	private BigDecimal calcTwoNum(BigDecimal currOperand, String currOperator, BigDecimal preCalcuResult) {
		// initiate the calculator result to 0
		BigDecimal calcuResult = BigDecimal.ZERO;
		// The default operator is +
		currOperator = (currOperator == null) ? "+" : currOperator;
		/**
		 * Note: begin to calculate according to inputing curOperator
		 */
		switch (currOperator) {
		case "+":
			calcuResult = preCalcuResult.add(currOperand);
			break;

		case "-":
			calcuResult = preCalcuResult.subtract(currOperand).setScale(precision, RoundingMode.HALF_UP);
			break;

		case "*":
			calcuResult = preCalcuResult.multiply(currOperand).setScale(precision, RoundingMode.HALF_UP);
			break;

		case "/":
			calcuResult = preCalcuResult.divide(currOperand, RoundingMode.HALF_UP);
			break;

		}

		return calcuResult;
	}
	
	

	/**
	 * Note: withdraw the last step
	 */
	public void undo() {
		if (preCalcuResult != null && recentOperatorIndex == -1) { // 未进行undo/redo操作，存储最后计算结果
			recentSerialResultList.add(preCalcuResult);
			currOperator = null;
			currOperand = null;
		}

		if (recentSerialResultList.size() == 0) {
			System.out.println("No any operation~ ");
		} else if (recentSerialResultList.size() == 1) {
			System.out.println("Before Undo: " + preCalcuResult + "After Undo: 0");
			preCalcuResult = BigDecimal.ZERO;
		} else {
			if (recentOperatorIndex == -1) {
				recentOperatorIndex = recentSerialOperatorList.size() - 1;
			} else {
				if (recentOperatorIndex - 1 < 0) {
					System.out.println("Notition: Can not to undo!");
					return;
				}
				recentOperatorIndex--;
			}
			cancelPreOperate(
					recentSerialResultList.get(recentOperatorIndex), 
					recentSerialOperatorList.get(recentOperatorIndex),
					recentSerialOperandList.get(recentOperatorIndex)
			);
		}
	}
	
	private void cancelPreOperate(BigDecimal lastTotal, String lastOpt, BigDecimal lastNum) {
		preCalcuResult = lastTotal;
		currOperator = null;
		currOperand = null;
		System.out.println("Before undo:" + preCalcuResult +  "; After undo:" + lastTotal + "; Undo Operator:" + lastOpt + "; Undo Operand:" + lastNum);
	}
	

	/**
	 * Note: Redo action according to withdraw
	 */
	public void redo() {
		try {
			if (recentOperatorIndex > -1) {
				if (recentOperatorIndex + 1 == recentSerialResultList.size() || recentOperatorIndex + 1 == this.maxValidIndex + 1) {
					System.out.println("Notition: Can not to undo!");
					return;
				}
				
				// Record the accumulation operation index value
				recentOperatorIndex++;

				redoOperate(
						recentSerialResultList.get(recentOperatorIndex), 
						recentSerialOperatorList.get(recentOperatorIndex - 1),
						recentSerialOperandList.get(recentOperatorIndex - 1)
				);
			}
			
		} catch (Exception e) {
			System.out.println("redo encouter exception, lastOptIndex: " + recentOperatorIndex);
		}
	}

	private void redoOperate(BigDecimal redoTotal, String redoOpt, BigDecimal redoNum) {
		preCalcuResult = redoTotal;
		currOperand = null;
		currOperator = null;
		System.out.println("Before redo: " + preCalcuResult + "; After redo: " + redoTotal + "; Redo Operator: " + redoOpt + "; Redo Operand:" + redoNum);
	}

	

	

	/**
	 * Note: display the calculator result
	 */
	public String display() {
		StringBuffer stringBuffer = new StringBuffer();

		if (preCalcuResult != null) {
			stringBuffer.append(preCalcuResult.setScale(precision, BigDecimal.ROUND_HALF_DOWN).toString());
		}
		if (currOperator != null) {
			stringBuffer.append(currOperator);
		}
		if (currOperand != null) {
			stringBuffer.append(currOperand);
		}
		System.out.println("The calculator process record: " + stringBuffer.toString());
		return stringBuffer.toString();
	}

	/**
	 * Test process 
	 */
	public static void main(String[] args) {
		Calculator calculator = new Calculator();
		calculator.setCurrOperand(new BigDecimal(9));
		calculator.setCurrOperator("+");
		calculator.setCurrOperand(new BigDecimal(4));
		calculator.display();
		calculator.execCalculator();
		calculator.display();
		calculator.setCurrOperator("*");
		calculator.setCurrOperand(new BigDecimal(3));
		calculator.display();
		calculator.execCalculator();
		calculator.display();
		calculator.undo();
		calculator.display();


		calculator.setCurrOperator("+");
		calculator.setCurrOperand(new BigDecimal(7));
		calculator.display();
		calculator.execCalculator();
		calculator.display();
		
		
		calculator.undo();
		calculator.display();

		calculator.redo();
		calculator.display();
	}

}


