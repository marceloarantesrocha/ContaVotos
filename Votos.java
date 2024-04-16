package com.mycompany.votos;

import java.io.*;
import java.util.*;

public class Votos {

    static String[] nomePartidos = {"Iniciativa Liberal", "Alternativa21", "Partido Socialista"};/*, "Livre",
            "Chega", "Ergue-te", "Nova Direita", "Bloco de Esquerda", "CDU", "PAN", "ADN", "Volt Portugal",
            "RIR", "Juntos pelo Povo", "Aliança Democrática"};*/

    static String[] circulosEleitorais = {"Aveiro", "Beja", "Braga"};/*, "Bragança",
            "Castelo Branco", "Coimbra", "Évora", "Faro", "Guarda", "Leiria", "Lisboa",
            "Portalegre", "Porto", "Santarém", "Setúbal",
            "Viana do Castelo", "Vila Real", "Viseu", "Açores",
            "Madeira", "Europa", "Fora da Europa"};*/

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String path = "C:\\Users\\marce\\Desktop\\votos\\"; // Diretório onde serão salvos os arquivos
        // Criando instâncias de VotosConcelho e VotosCirculoEleitoral
        Map<String, Integer> totalNacional = new HashMap<>();
        Map<String, Integer> totalPorCirculo = new HashMap<>(); // Mapa para armazenar o total de votos por círculo eleitoral

        for (String concelho : circulosEleitorais) {
            VotosConcelho votosConcelho = new VotosConcelho(concelho);
            int votosValidos = 0; // Variável para armazenar a soma dos votos válidos
            for (String partido : nomePartidos) {
                System.out.println("Digite os votos para o partido " + partido + " em " + concelho + ":");
                int votos = scanner.nextInt();
                votosConcelho.adicionarVotos(partido, votos);
                totalNacional.put(partido, totalNacional.getOrDefault(partido, 0) + votos);
                votosValidos += votos; // Adicionando os votos ao total de votos válidos
                totalPorCirculo.put(concelho, totalPorCirculo.getOrDefault(concelho, 0) + votos); // Atualizando o total de votos por círculo eleitoral
            }
            salvarVotosConcelhoComoTXT(path, concelho, votosValidos, votosConcelho); // Salvar votos válidos na primeira linha
            VotosCirculoEleitoral votosCirculo = new VotosCirculoEleitoral(concelho);
            votosCirculo.adicionarVotosConcelho(concelho, votosConcelho);
            serializarObjeto(votosCirculo, path); // Serializar o objeto VotosCirculoEleitoral
        }

        // Criar e escrever no arquivo TotalNacional.txt
        escreverTotalNacional(totalNacional, path);

        // Deserializar e mostrar resultados
        for (String concelho : circulosEleitorais) {
            VotosCirculoEleitoral votosCirculoDeserializado = deserializarObjeto(path + concelho + ".dat");
            mostrarResultadoOrdenado(votosCirculoDeserializado, totalPorCirculo.get(concelho)); // Passar o total de votos por círculo eleitoral
        }
    }

    // Método para serializar um objeto em arquivo binário
    public static void serializarObjeto(VotosCirculoEleitoral votosCirculo, String path) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(path + votosCirculo.getNomeCirculo() + ".dat"))) {
            out.writeObject(votosCirculo);
            System.out.println("Objeto serializado com sucesso: " + votosCirculo.getNomeCirculo() + ".dat");
        } catch (IOException e) {
            System.err.println("Erro ao serializar objeto: " + e.getMessage());
        }
    }

    // Método para deserializar um objeto a partir de um arquivo binário
    public static VotosCirculoEleitoral deserializarObjeto(String fileName) {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(fileName))) {
            VotosCirculoEleitoral votosCirculo = (VotosCirculoEleitoral) in.readObject();
            System.out.println("Objeto deserializado com sucesso: " + votosCirculo.getNomeCirculo() + ".dat");
            return votosCirculo;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Erro ao deserializar objeto: " + e.getMessage());
            return null;
        }
    }

    // Método para mostrar o resultado na tela ordenado por número de votos
    public static void mostrarResultadoOrdenado(VotosCirculoEleitoral votosCirculo, int totalVotosCirculo) {
        System.out.println("Resultado dos votos por círculo eleitoral:");
        System.out.println("Círculo Eleitoral: " + votosCirculo.getNomeCirculo());

        for (Map.Entry<String, VotosConcelho> entry : votosCirculo.getVotosPorConcelho().entrySet()) {
            String concelho = entry.getKey();
            VotosConcelho votosConcelho = entry.getValue();

            System.out.println("Concelho: " + concelho);
            System.out.println("Votos por partido (em ordem decrescente):");
            List<Map.Entry<String, Integer>> sortedVotos = new ArrayList<>(votosConcelho.getVotosPorPartido().entrySet());
            Collections.sort(sortedVotos, (a, b) -> b.getValue().compareTo(a.getValue())); // Ordenar por número de votos em ordem decrescente

            for (Map.Entry<String, Integer> voto : sortedVotos) {
                String partido = voto.getKey();
                int votos = voto.getValue();
                double percentagem = (double) votos / totalVotosCirculo * 100; // Calcular a porcentagem em relação ao total de votos do círculo eleitoral
                System.out.printf("%s: %d votos (%.2f%%)\n", partido, votos, percentagem);
            }
            System.out.println();
        }
    }

    // Método para salvar os votos de um concelho em um arquivo de texto (.txt)
    public static void salvarVotosConcelhoComoTXT(String path, String concelho, int votosValidos, VotosConcelho votosConcelho) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(path + concelho + ".txt"))) {
            writer.println("Votos válidos: " + votosValidos + "\n"); // Escrever total de votos válidos na primeira linha
            for (Map.Entry<String, Integer> entry : votosConcelho.getVotosPorPartido().entrySet()) {
                String partido = entry.getKey();
                int votos = entry.getValue();
                double percentagem = (double) votos / votosValidos * 100; // Calcular a porcentagem
                writer.printf("%s: %d votos (%.2f%%)\n", partido, votos, percentagem);
            }
            System.out.println("Arquivo " + concelho + ".txt criado com sucesso.");
        } catch (IOException e) {
            System.err.println("Erro ao salvar votos do concelho " + concelho + " em arquivo de texto: " + e.getMessage());
        }
    }

    // Método para escrever o total de votos nacionais em um arquivo TotalNacional.txt
    public static void escreverTotalNacional(Map<String, Integer> totalNacional, String path) {
        int votosValidosTotal = totalNacional.values().stream().mapToInt(Integer::intValue).sum(); // Calcular o total de votos válidos
        try (PrintWriter writer = new PrintWriter(new FileWriter(path + "TotalNacional.txt"))) {
            writer.println("Votos válidos total: " + votosValidosTotal + "\n"); // Escrever total de votos válidos na primeira linha
            List<Map.Entry<String, Integer>> sortedTotal = new ArrayList<>(totalNacional.entrySet());
            Collections.sort(sortedTotal, (a, b) -> b.getValue().compareTo(a.getValue())); // Ordenar por número de votos em ordem decrescente
            for (Map.Entry<String, Integer> entry : sortedTotal) {
                String partido = entry.getKey();
                int votos = entry.getValue();
                double percentagem = (double) votos / votosValidosTotal * 100; // Calcular a porcentagem
                writer.printf("%s: %d votos (%.2f%%)\n", partido, votos, percentagem);
            }
            System.out.println("Arquivo TotalNacional.txt criado com sucesso.");
        } catch (IOException e) {
            System.err.println("Erro ao criar arquivo TotalNacional.txt: " + e.getMessage());
        }
    }
}
