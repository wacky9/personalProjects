package simulator;

import java.util.Scanner;
import simulator.ErrorHandler.ERROR_TYPE;

/**
 * Processes different opcodes and includes functionality for executing a cycle.
 *
 */
public class Interpreter {

    /**
     * The maximum value memory can hold.
     */
    private final static int MEM_MAX = 65536;
    
    /**
     * Mask to get value in lower 8 bits.
     */
    private final static int LowerEightBitsMask = 255;

    /**
     * Simulates the instruction process cycle. Uses the program counter to
     * fetch the current instruction from memory. From that instruction, we can
     * decode the data (like the registers, vectors, etc) of the instruction.
     * Then, the instruction can be executed, result stored, and condition codes
     * updated.
     *
     * @param mem
     *            Created object to represent the memory of the machine. The
     *            object is built on an array of shorts since each memory space
     *            is 16 bits
     * @param reg
     *            An array of register objects not including the program counter
     *            since it behaviors differently than the other registers.
     * @param cond
     *            An array of special register objects. 1 means the register is
     *            set and every other value means it is not set. cond[0] is the
     *            negative condition, cond[1] is the zero condition, and cond[2]
     *            is the positive condition.
     * @param pc
     *            The address of the program counter.
     * @return An enum telling the simulator what instruction was executed
     */
    public static Instructions executeCycle(MainMemory mem,
            GeneralRegister[] reg, ConditionRegister[] cond,
            ProgramCounter pc) {

        //Fetch the instruction from memory and increment
        short instr = (short) pc.getAddress();
        pc.increment();

        //Decode the instruction
        short[] info = mem.getInfo(instr);
        //Execute the instruction. Evaluate addresses and operands before execution
        return execute(info, reg, mem, pc, cond);
    }

    /**
     * Starts execution of an individual instruction by looking in the info
     * array. The first element of this array is a 4 bit code that allows us to
     * figure out which instruction will be executed. Also allows us to figure
     * out what the rest of the elements in the info array are and which ones
     * need to be passed.
     *
     * @param info
     *            An array of shorts contain information from the instruction
     * @param mem
     *            Created object to represent the memory of the machine. The
     *            object is built on an array of shorts since each memory space
     *            is 16 bits
     * @param reg
     *            An array of register objects not including the program counter
     *            since it behaviors differently than the other registers.
     * @param cond
     *            An array of register objects. 1 means the register is set and
     *            every other value means it is not set. cond[0] is the negative
     *            condition, cond[1] is the zero condition, and cond[2] is the
     *            positive condition.
     * @param pc
     *            The address of the program counter.
     * @return An enum telling the simulator what instruction was executed
     */
    private static Instructions execute(short[] info, GeneralRegister[] reg,
            MainMemory mem, ProgramCounter pc, ConditionRegister[] cond) {

        //info[0] is the name of the opcode
        switch (info[0]) {
            case 0b0001:
                return add(info, cond, reg, mem);
            case 0b0101:
                return and(info, cond, reg, mem);
            case 0b0000:
                return brx(info, cond, reg, mem, pc);
            case 0b1000:
                //Debug instruction will be in Simulator since it needs that for tracing mode
                return Instructions.DBUG;
            case 0b0100:
                return jsr(info, cond, reg, mem, pc);
            case 0b1100:
                return jsrr(info, cond, reg, mem, pc);
            case 0b0010:
                return ld(info, cond, reg, mem, pc);
            case 0b1010:
                return ldi(info, cond, reg, mem, pc);
            case 0b0110:
                return ldr(info, cond, reg, mem);
            case 0b1110:
                return lea(info, cond, reg, mem, pc);
            case 0b1001:
                return not(info, cond, reg, mem);
            case 0b1101:
                return ret(info, cond, reg, mem, pc);
            case 0b0011:
                return st(info, cond, reg, mem, pc);
            case 0b1011:
                return sti(info, cond, reg, mem, pc);
            case 0b0111:
                return str(info, cond, reg, mem);
            case 0b1111:
                return trap(info, cond, reg, mem, pc);
            default:
                //Should never reach here as all possible 4 bit values are covered
                ErrorHandler.queueError(ERROR_TYPE.INSTRUCTION_PARSE_ERROR);
                return Instructions.DBUG;
        }
    }

    /**
     * Updates the condition codes based on the result of the operation. Not all
     * operations change the condition codes. Only those that process
     * operations, load data, or the trap instruction.
     *
     * @param result
     *            Value that was stored to a register or loaded from memory
     * @param cond
     *            An array of shorts with that details data that changed due to
     *            the instruction. cond[1] = N cond, cond[2] = Z cond, and
     *            cond[3] = P cond.
     */
    public static void updateConds(int result, ConditionRegister[] cond) {
        if (result < 0) {
            cond[0].setVal((short) 1);
            cond[1].setVal((short) 0);
            cond[2].setVal((short) 0);
        }
        if (result == 0) {
            cond[0].setVal((short) 0);
            cond[1].setVal((short) 1);
            cond[2].setVal((short) 0);
        }
        if (result > 0) {
            cond[0].setVal((short) 0);
            cond[1].setVal((short) 0);
            cond[2].setVal((short) 1);
        }
    }
    //Below is data processing operations
    //All of these instructions modify the condition registers

    /**
     * Adds two operands. Includes conditional logic to figure out if a register
     * or an immediate is being used for the second operand. Store to a
     * register.
     *
     * @param info
     *            An array of shorts contain information from the instruction
     * @param mem
     *            Object representing memory
     * @param reg
     *            An array of register objects
     * @param cond
     *            An array of special register objects.
     * @return An enum telling the simulator what instruction was executed
     */
    public static Instructions add(short[] info, ConditionRegister[] cond,
            GeneralRegister[] reg, MainMemory mem) {
        short op1 = reg[info[2]].getVal();

        //Determines if the second operand is a register or an immediate
        short op2 = 0;
        if (info[3] == 0) {
            op2 = reg[info[4]].getVal();
        } else {
            op2 = Bits.signExtend(info[4]);
        }

        int result = op1 + op2;

        if (result > MEM_MAX) {
            ErrorHandler.queueError(ERROR_TYPE.INTERPRETER_ADDITION_OVERFLOW);
        }
        reg[info[1]].setVal((short) result);
        updateConds((short) result, cond);
        return Instructions.ADD;
    }

    /**
     * Bitwise ands two operands together. Includes conditional logic to figure
     * out if a register or an immediate is being used for the second operand.
     * Store to a register.
     *
     * @param info
     *            An array of shorts contain information from the instruction
     * @param mem
     *            Object representing memory
     * @param reg
     *            An array of register objects
     * @param cond
     *            An array of special register objects.
     * @return An enum telling the simulator what instruction was executed
     */
    public static Instructions and(short[] info, ConditionRegister[] cond,
            GeneralRegister[] reg, MainMemory mem) {
        short op1 = reg[info[2]].getVal();

        //Determines if the second operand is a register or an immediate
        short op2 = 0;
        if (info[3] == 0) {
            op2 = reg[info[4]].getVal();
        } else {
        	op2 = Bits.signExtend(info[4]);
        }
        int result = op1 & op2;

        reg[info[1]].setVal((short) result);
        updateConds(result, cond);
        return Instructions.AND;
    }

    /**
     * Takes a register and bitwise not its value. Stores to a register.
     *
     * @param info
     *            An array of shorts contain information from the instruction
     * @param mem
     *            Object representing memory
     * @param reg
     *            An array of register objects
     * @param cond
     *            An array of special register objects.
     * @return An enum telling the simulator what instruction was executed
     */
    public static Instructions not(short[] info, ConditionRegister[] cond,
            GeneralRegister[] reg, MainMemory mem) {
        short op1 = reg[info[2]].getVal();
        int result = ~op1;

        reg[info[1]].setVal((short) result);
        updateConds(result, cond);
        return Instructions.NOT;
    }

    //Below is data movement operations
    //The load instructions modify the CCRs while the store instructions do not.

    /**
     * Loads an operand to a register in direct addressing mode.
     *
     * @param info
     *            An array of shorts contain information from the instruction
     * @param mem
     *            Object representing memory
     * @param reg
     *            An array of register objects
     * @param cond
     *            An array of special register objects.
     * @param pc
     *            The address of the program counter.
     * @return An enum telling the simulator what instruction was executed
     */
    public static Instructions ld(short[] info, ConditionRegister[] cond,
            GeneralRegister[] reg, MainMemory mem, ProgramCounter pc) {
        int direct = Bits.fullAddress((short) (pc.getAddress()), info[2]);
        reg[info[1]].setVal(mem.readFromMemory((short) direct));
        updateConds(reg[info[1]].getVal(), cond);
        return Instructions.LD;
    }

    /**
     * Loads an operand to a register in register indexed addressing mode.
     *
     * @param info
     *            An array of shorts contain information from the instruction
     * @param mem
     *            Object representing memory
     * @param reg
     *            An array of register objects
     * @param cond
     *            An array of special register objects.
     * @return An enum telling the simulator what instruction was executed
     */
    public static Instructions ldr(short[] info, ConditionRegister[] cond,
            GeneralRegister[] reg, MainMemory mem) {
        int indexed = Bits.indexAddress(reg[info[2]].getVal(), (info[3]));
        reg[info[1]].setVal(mem.readFromMemory((short) indexed));
        updateConds(reg[info[1]].getVal(), cond);
        return Instructions.LDR;
    }

    /**
     * Loads an address to a register in indirect addressing mode.
     *
     * @param info
     *            An array of shorts contain information from the instruction
     * @param mem
     *            Object representing memory
     * @param reg
     *            An array of register objects
     * @param cond
     *            An array of special register objects.
     * @param pc
     *            The address of the program counter.
     * @return An enum telling the simulator what instruction was executed
     */
    public static Instructions ldi(short[] info, ConditionRegister[] cond,
            GeneralRegister[] reg, MainMemory mem, ProgramCounter pc) {
        int indirect = Bits.fullAddress((short) (pc.getAddress()), info[2]);
        reg[info[1]].setVal(mem.readFromMemory((short) indirect));
        updateConds(reg[info[1]].getVal(), cond);
        return Instructions.LDI;
    }

    /**
     * Loads an address to a register in immediate addressing mode.
     *
     * @param info
     *            An array of shorts contain information from the instruction
     * @param mem
     *            Object representing memory
     * @param reg
     *            An array of register objects
     * @param cond
     *            An array of special register objects.
     * @param pc
     *            The address of the program counter.
     * @return An enum telling the simulator what instruction was executed
     */
    public static Instructions lea(short[] info, ConditionRegister[] cond,
            GeneralRegister[] reg, MainMemory mem, ProgramCounter pc) {
        int immediate = Bits.fullAddress((short) (pc.getAddress()), info[2]);
        reg[info[1]].setVal((short) immediate);
        updateConds(immediate, cond);
        return Instructions.LEA;
    }

    /**
     * Stores an operand to memory in immediate addressing mode.
     *
     * @param info
     *            An array of shorts contain information from the instruction
     * @param mem
     *            Object representing memory
     * @param reg
     *            An array of register objects
     * @param cond
     *            An array of special register objects.
     * @param pc
     *            The address of the program counter.
     * @return An enum telling the simulator what instruction was executed
     */
    public static Instructions st(short[] info, ConditionRegister[] cond,
            GeneralRegister[] reg, MainMemory mem, ProgramCounter pc) {
        int direct = Bits.fullAddress((short) (pc.getAddress()), info[2]);
        ;
        mem.writeToMemory((short) direct, reg[info[1]].getVal());
        return Instructions.ST;
    }

    /**
     * Stores an operand to memory in register indexed addressing mode.
     *
     * @param info
     *            An array of shorts contain information from the instruction
     * @param mem
     *            Object representing memory
     * @param reg
     *            An array of register objects
     * @param cond
     *            An array of special register objects.
     * @return An enum telling the simulator what instruction was executed
     */
    public static Instructions str(short[] info, ConditionRegister[] cond,
            GeneralRegister[] reg, MainMemory mem) {
        int indexed = Bits.indexAddress(reg[info[2]].getVal(), (info[3]));
        mem.writeToMemory((short) indexed, reg[info[1]].getVal());
        return Instructions.STR;
    }

    /**
     * Stores an address to memory in register indexed addressing mode.
     *
     * @param info
     *            An array of shorts contain information from the instruction
     * @param mem
     *            Object representing memory
     * @param reg
     *            An array of register objects
     * @param cond
     *            An array of special register objects.
     * @param pc
     *            The address of the program counter.
     * @return An enum telling the simulator what instruction was executed
     */
    public static Instructions sti(short[] info, ConditionRegister[] cond,
            GeneralRegister[] reg, MainMemory mem, ProgramCounter pc) {
        int indirect = Bits.fullAddress((short) (pc.getAddress()), info[2]);
        ;
        mem.writeToMemory(mem.readFromMemory((short) indirect),
                reg[info[1]].getVal());
        return Instructions.STI;
    }

    //Below is the flow control instructions
    //Only TRAP might modify the CCRs

    /**
     * If any of the condition registers are set to 1, jump to an address. Sets
     * a new program counter
     *
     * @param info
     *            An array of shorts contain information from the instruction
     * @param mem
     *            Object representing memory
     * @param reg
     *            An array of register objects
     * @param cond
     *            An array of special register objects.
     * @param pc
     *            The address of the program counter.
     * @return An enum telling the simulator what instruction was executed
     */
    public static Instructions brx(short[] info, ConditionRegister[] cond,
            GeneralRegister[] reg, MainMemory mem, ProgramCounter pc) {

        if ((cond[0].getVal() == 1 && info[1] == 1)
                || (cond[1].getVal() == 1 && info[2] == 1)
                || (cond[2].getVal() == 1) && info[3] == 1) {
            short address = Bits.fullAddress((short) (pc.getAddress()),
                    info[4]);
            ;
            pc.setAddress(address);
        }
        return Instructions.BRX;
    }

    /**
     * Uses direct addressing mode to jump to an address. Sets a new program
     * counter
     *
     * @param info
     *            An array of shorts contain information from the instruction
     * @param mem
     *            Object representing memory
     * @param reg
     *            An array of register objects
     * @param cond
     *            An array of special register objects.
     * @param pc
     *            The address of the program counter.
     * @return An enum telling the simulator what instruction was executed
     */
    public static Instructions jsr(short[] info, ConditionRegister[] cond,
            GeneralRegister[] reg, MainMemory mem, ProgramCounter pc) {

        //Instruction to return at end
        Instructions instr = Instructions.JSR;

        //info[1] is the link bit
        if (info[1] == 1) {
            int direct = Bits.fullAddress((short) (pc.getAddress()), info[2]);
            reg[7].setVal((short) pc.getAddress());
            pc.setAddress((short) direct);
        } else {
            //Do not want to save old pc
            instr = jmp(info, cond, reg, mem, pc);
        }
        return instr;
    }

    /**
     * Uses register index addressing mode to jump to an address. Sets a new
     * program counter
     *
     * @param info
     *            An array of shorts contain information from the instruction
     * @param mem
     *            Object representing memory
     * @param reg
     *            An array of register objects
     * @param cond
     *            An array of special register objects.
     * @param pc
     *            The address of the program counter.
     * @return An enum telling the simulator what instruction was executed
     */
    public static Instructions jsrr(short[] info, ConditionRegister[] cond,
            GeneralRegister[] reg, MainMemory mem, ProgramCounter pc) {

        //Instruction to return at end
        Instructions instr = Instructions.JSRR;

        //info[1] is the link bit
        if (info[1] == 1) {
            int indexed = Bits.indexAddress(reg[info[2]].getVal(), (info[3]));
            reg[7].setVal((short) pc.getAddress());
            pc.setAddress((short) indexed);
        } else {
            //Do not want save old pc
            instr = jmpr(info, cond, reg, mem, pc);
        }
        return instr;
    }

    /**
     * Uses register index addressing mode to jump to an address. Does not set a
     * new program counter
     *
     * @param info
     *            An array of shorts contain information from the instruction
     * @param mem
     *            Object representing memory
     * @param reg
     *            An array of register objects
     * @param cond
     *            An array of special register objects.
     * @param pc
     *            The address of the program counter.
     * @return An enum telling the simulator what instruction was executed
     */
    public static Instructions jmp(short[] info, ConditionRegister[] cond,
            GeneralRegister[] reg, MainMemory mem, ProgramCounter pc) {

        int address = Bits.fullAddress((short) (pc.getAddress()), info[2]);
        pc.setAddress((short) address);
        return Instructions.JMP;
    }

    /**
     * Uses register index addressing mode to jump to an address. Does not set a
     * new program counter
     *
     * @param info
     *            An array of shorts contain information from the instruction
     * @param mem
     *            Object representing memory
     * @param reg
     *            An array of register objects
     * @param cond
     *            An array of special register objects.
     * @param pc
     *            The address of the program counter.
     * @return An enum telling the simulator what instruction was executed
     */
    public static Instructions jmpr(short[] info, ConditionRegister[] cond,
            GeneralRegister[] reg, MainMemory mem, ProgramCounter pc) {

        int indexed = Bits.indexAddress(reg[info[2]].getVal(), (info[3]));
        pc.setAddress((short) indexed);
        return Instructions.JMPR;
    }

    /**
     * When returning from a subroutine, returns the address of the next
     * instruction in the caller function (pc).
     *
     * @param info
     *            An array of shorts contain information from the instruction
     * @param mem
     *            Object representing memory
     * @param reg
     *            An array of register objects
     * @param cond
     *            An array of special register objects.
     * @param pc
     *            The address of the program counter.
     * @return An enum telling the simulator what instruction was executed
     */
    public static Instructions ret(short[] info, ConditionRegister[] cond,
            GeneralRegister[] reg, MainMemory mem, ProgramCounter pc) {
        pc.setAddress(reg[7].getVal());
        return Instructions.RET;
    }

    /**
     * Executes various system calls depending on a vector included in the
     * instruction. Can change condition code registers.
     *
     * @param info
     *            An array of shorts contain information from the instruction
     * @param mem
     *            Object representing memory
     * @param reg
     *            An array of register objects
     * @param cond
     *            An array of special register objects.
     * @param pc
     * 			  The program counter.
     * 
     * @return An enum telling the simulator what instruction was executed
     */
    public static Instructions trap(short[] info, ConditionRegister[] cond,
            GeneralRegister[] reg, MainMemory mem, ProgramCounter pc) {

        switch (info[1]) {
            case (0x21):
                System.out.println(
                        "Character in register 0: " + ((char)reg[0].getVal() & 255));
                reg[7].setVal((short) pc.getAddress());
                return Instructions.OUT;
            case (0x22):
                short address = reg[0].getVal();
                int next = mem.readFromMemory(reg[0].getVal());
                //Only want the first 8 bits for the char
                next = next & LowerEightBitsMask;
                char nextChar = (char) next;
                //0 is the ASCII character for null
                while (nextChar != 0) {
                    System.out.print(nextChar);
                    address++;
                    next = mem.readFromMemory(address);

                    //Only want the first 8 bits for the char
                    next = next & LowerEightBitsMask;
                    nextChar = (char) next;
                }

                //Moves to a new line
                System.out.println("");
                reg[7].setVal((short) pc.getAddress());
                return Instructions.PUTS;
            case (0x23):
                System.out.print("Please enter an ASCII character: ");
                Scanner input1 = new Scanner(System.in);
                char letter = input1.next().charAt(0);

                //Disregard the rest of the input
                input1.nextLine();
                System.out.println("Entered:" + letter);

                //ASCII characters are only 8 bits
                if (letter <= LowerEightBitsMask) {

                    //Clear register
                    reg[0].setVal((short) 0);
                    reg[0].setVal((short) letter);
                    updateConds(letter, cond);
                } else {
                    ErrorHandler
                            .queueError(ERROR_TYPE.INTERPRETER_INVALID_CHAR);
                }
                reg[7].setVal((short) pc.getAddress());
                return Instructions.IN;
            case (0x25):
                reg[7].setVal((short) pc.getAddress());
                return Instructions.HALT;
            case (0x31):
                System.out.println(
                        "Decimal value in register 0: " + reg[0].getVal());
                reg[7].setVal((short) pc.getAddress());
                return Instructions.OUTN;
            case (0x33):
                Scanner input2 = new Scanner(System.in);
                System.out.print(
                        "Please enter a base 10 number to input. Max is 32767 and min is -32768 inclusive: ");
                String numStr = input2.nextLine();
                short num = 0;
                try {
                    num = Short.parseShort(numStr);
                } catch (Exception e) {
                    ErrorHandler.queueError(ERROR_TYPE.INTERPRETER_INVALID_INT);
                    reg[7].setVal((short) pc.getAddress());
                    return Instructions.INN;
                }
                System.out.println("Entered:" + num);
                reg[0].setVal(num);
                updateConds(num, cond);
                reg[7].setVal((short) pc.getAddress());
                return Instructions.INN;
            case (0x43):
                reg[0].randomize();
                updateConds(reg[0].getVal(), cond);
                reg[7].setVal((short) pc.getAddress());
                return Instructions.RND;
            default:
                ErrorHandler
                        .queueError(ERROR_TYPE.INTERPRETER_INVALID_TRAP_VECTOR);
                return Instructions.DBUG;
        }
    }
}
