package md.mirrerror;

import java.util.BitSet;
import java.util.Scanner;
import java.util.Random;

public class DESKeyGenerator {

    private static final int[] PC1 = {
            57, 49, 41, 33, 25, 17, 9,
            1, 58, 50, 42, 34, 26, 18,
            10, 2, 59, 51, 43, 35, 27,
            19, 11, 3, 60, 52, 44, 36,
            63, 55, 47, 39, 31, 23, 15,
            7, 62, 54, 46, 38, 30, 22,
            14, 6, 61, 53, 45, 37, 29,
            21, 13, 5, 28, 20, 12, 4
    };

    private static final int[] PC2 = {
            14, 17, 11, 24, 1, 5,
            3, 28, 15, 6, 21, 10,
            23, 19, 12, 4, 26, 8,
            16, 7, 27, 20, 13, 2,
            41, 52, 31, 37, 47, 55,
            30, 40, 51, 45, 33, 48,
            44, 49, 39, 56, 34, 53,
            46, 42, 50, 36, 29, 32
    };

    private static final int[] SHIFT_SCHEDULE = {
            1, 1, 2, 2, 2, 2, 2, 2,
            1, 2, 2, 2, 2, 2, 2, 1
    };

    public static void main(String[] args) {
        System.out.println("Используемые таблицы:");
        System.out.println("PC-1: " + printTable(PC1));
        System.out.println("PC-2: " + printTable(PC2));
        System.out.println("SHIFT_SCHEDULE: " + printArray(SHIFT_SCHEDULE));

        Scanner scanner = new Scanner(System.in);
        System.out.println("Введите 8-символьный ключ DES или оставьте пустым для случайного генерации:");
        String keyInput = scanner.nextLine();

        byte[] keyBytes;
        if (keyInput.isEmpty()) {
            keyBytes = generateRandomKey();
            System.out.println("Сгенерированный случайный ключ: " + byteArrayToHexString(keyBytes));
        } else {
            keyBytes = keyInput.getBytes();
            if (keyBytes.length != 8) {
                System.out.println("Ключ должен быть длиной 8 символов!");
                return;
            }
        }

        BitSet initialKey = BitSet.valueOf(keyBytes);
        System.out.println("Начальный ключ (64 бита): " + bitSetToString(initialKey, 64));

        BitSet permutedKey = permute(initialKey, PC1, 56);
        System.out.println("После перестановки PC-1 (56 бит): " + bitSetToString(permutedKey, 56));

        BitSet[] roundKeys = generateRoundKeys(permutedKey);
        for (int i = 0; i < roundKeys.length; i++) {
            System.out.println("Раундовый ключ K" + (i + 1) + ": " + bitSetToString(roundKeys[i], 48));
        }

        scanner.close();
    }

    private static byte[] generateRandomKey() {
        byte[] key = new byte[8];
        new Random().nextBytes(key);
        return key;
    }

    private static BitSet permute(BitSet key, int[] table, int size) {
        BitSet permuted = new BitSet(size);
        for (int i = 0; i < size; i++) {
            permuted.set(i, key.get(table[i] - 1));
        }
        return permuted;
    }

    private static BitSet[] generateRoundKeys(BitSet permutedKey) {
        BitSet left = permutedKey.get(0, 28);
        BitSet right = permutedKey.get(28, 56);
        BitSet[] roundKeys = new BitSet[16];

        for (int i = 0; i < 16; i++) {
            System.out.println("\nРаунд " + (i + 1) + ":");
            System.out.println("Левая часть до сдвига:  " + bitSetToString(left, 28));
            System.out.println("Правая часть до сдвига: " + bitSetToString(right, 28));
            System.out.println("Количество сдвигов: " + SHIFT_SCHEDULE[i]);

            left = rotateLeft(left, 28, SHIFT_SCHEDULE[i]);
            right = rotateLeft(right, 28, SHIFT_SCHEDULE[i]);

            System.out.println("Левая часть после сдвига:  " + bitSetToString(left, 28));
            System.out.println("Правая часть после сдвига: " + bitSetToString(right, 28));

            BitSet combined = new BitSet(56);
            for (int j = 0; j < 28; j++) {
                combined.set(j, left.get(j));
                combined.set(j + 28, right.get(j));
            }

            roundKeys[i] = permute(combined, PC2, 48);
            System.out.println("Раундовый ключ после PC-2: " + bitSetToString(roundKeys[i], 48));
        }
        return roundKeys;
    }

    private static BitSet rotateLeft(BitSet bits, int size, int shifts) {
        BitSet rotated = new BitSet(size);
        for (int i = 0; i < size; i++) {
            rotated.set(i, bits.get((i + shifts) % size));
        }
        return rotated;
    }

    private static String bitSetToString(BitSet bitSet, int size) {
        StringBuilder sb = new StringBuilder(size);
        for (int i = 0; i < size; i++) {
            sb.append(bitSet.get(i) ? '1' : '0');
        }
        return sb.toString();
    }

    private static String byteArrayToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    private static String printTable(int[] table) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < table.length; i++) {
            sb.append(table[i]);
            if (i < table.length - 1) sb.append(", ");
            if ((i + 1) % 7 == 0) sb.append("\n");
        }
        return sb.toString();
    }

    private static String printArray(int[] array) {
        StringBuilder sb = new StringBuilder();
        for (int value : array) {
            sb.append(value).append(" ");
        }
        return sb.toString().trim();
    }

}
