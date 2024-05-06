package br.com.alura.screenmatch.principal;

import br.com.alura.screenmatch.model.*;
import br.com.alura.screenmatch.repository.SerieRepository;
import br.com.alura.screenmatch.service.ConsumoApi;
import br.com.alura.screenmatch.service.ConverteDados;

import java.util.*;
import java.util.stream.Collectors;

public class Principal {

    private Scanner leitura = new Scanner(System.in);
    private ConsumoApi consumo = new ConsumoApi();
    private ConverteDados conversor = new ConverteDados();
    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=6585022c";

    private SerieRepository repository;

    private List<Serie> series = new ArrayList<>();

    private List<DadosSerie> dadosSerie = new ArrayList<>();

    private Optional<Serie> serieBusca;

    public Principal(SerieRepository repository) {
        this.repository = repository;
    }

    public void exibeMenu() {

        var opcao = -1;
        while(opcao != 0) {
            var menu = """
                    1 - Buscar séries
                    2 - Buscar episódios
                    3 - Listar series buscadas
                    4 - Buscar Serie por Titulo
                    5 - Buscar por ator
                    6 - Buscar por ator e avaliacao
                    7 - Buscar as 5 Top Series
                    8 - Buscar series por categoria
                    9 - Buscar series por qtd de temporada e avaliacao
                    10 - Buscar episodio por trecho
                    11 - Buscar top episodios por serie
                    12 - Buscar episodios a partir de uma data
                    0 - Sair
                    """;

            System.out.println(menu);
            opcao = leitura.nextInt();
            leitura.nextLine();

            switch (opcao) {
                case 1:
                    buscarSerieWeb();
                    break;
                case 2:
                    buscarEpisodioPorSerie();
                    break;
                case 3:
                    listarSeriesBuscadas();
                    break;
                case 4:
                    buscarSeriePorTitulo();
                    break;
                case 5:
                    buscarSeriePorAtor();
                    break;
                case 6:
                    buscarSeriePorAtorEAvaliacao();
                    break;
                case 7:
                    buscarTop5Series();
                case 8:
                    buscarSeriesPorCategoria();
                    break;
                case 9:
                    filtrarSeriesPorTemporadaEAvaliacao();
                    break;
                case 10:
                    buscarEpisodioPorTrecho();
                    break;
                case 11:
                    buscarTopEpisodioPorSerie();
                    break;
                case 12:
                    buscarEpisodiosAPartirDeUmaData();
                    break;
                case 0:
                    System.out.println("Saindo...");
                    break;
                default:
                    System.out.println("Opção inválida");
            }
        }
    }

    private void buscarSerieWeb() {


        DadosSerie dados = getDadosSerie();
        //dadosSerie.add(dados);

        if(dados != null){
            Serie serie = new Serie(dados);
            repository.save(serie);
            System.out.println(dados);
        }
    }

    private DadosSerie getDadosSerie() {
        System.out.println("Digite o nome da série para busca");
        var nomeSerie = leitura.nextLine();
        DadosSerie dados = null;

        var verifica = repository.findByTituloContainingIgnoreCase(nomeSerie);

        if(verifica.isPresent()){
            System.out.printf("\nSerie ja cadastrada no banco!\n");
        } else {
        var json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);
        dados = conversor.obterDados(json, DadosSerie.class);

        }
        return dados;
    }

    private void buscarEpisodioPorSerie(){
        //DadosSerie dadosSerie = getDadosSerie();
        listarSeriesBuscadas();
        System.out.printf("Escolha uma serie pelo nome: ");
        var nomeSerie = leitura.nextLine();

//        Optional<Serie> serie = series.stream()
//                .filter(s -> s.getTitulo().toLowerCase().contains(nomeSerie.toLowerCase()))
//                .findFirst();

        Optional<Serie> serie = repository.findByTituloContainingIgnoreCase(nomeSerie);

        if(serie.isPresent()) {

            var serieEncontrada = serie.get();

            List<DadosTemporada> temporadas = new ArrayList<>();

            for (int i = 1; i <= serieEncontrada.getTotalTemporadas(); i++) {
                var json = consumo.obterDados(ENDERECO + serieEncontrada.getTitulo().replace(" ", "+") + "&season=" + i + API_KEY);
                DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
                temporadas.add(dadosTemporada);
            }
            temporadas.forEach(System.out::println);

            List<Episodio> episodios = temporadas.stream()
                    .flatMap(d -> d.episodios().stream()
                            .map(e -> new Episodio(d.numero(), e)))
                    .collect(Collectors.toList());

            serieEncontrada.setEpisodios(episodios);
            repository.save(serieEncontrada);

        } else {
            System.out.printf("Serie nao encontrada!");
        }
    }

    private void listarSeriesBuscadas(){

    series = repository.findAll();
//        series = dadosSerie.stream()
//                        .map(d -> new Serie(d))
//                                .collect(Collectors.toList());
        series.stream()
                .sorted(Comparator.comparing(Serie::getGenero))
                .forEach(System.out::println);

        dadosSerie.forEach(System.out::println);
    }

    private void buscarSeriePorTitulo(){
        System.out.printf("Escolha uma serie pelo nome: ");
        var nomeSerie = leitura.nextLine();
        serieBusca = repository.findByTituloContainingIgnoreCase(nomeSerie);

        if(serieBusca.isPresent()){
            System.out.printf("Dados da serie: " + serieBusca.get());
        }else {
            System.out.printf("Serie nao encontrada.");
        }
    }

    private void buscarSeriePorAtor(){
        System.out.printf("Qual o nome para busca?");
        var nomeAtor = leitura.nextLine();
        List<Serie> seriesEncontradas = repository.findbyAtoresContainingIgnoreCase(nomeAtor);
        System.out.printf("Series em que o ator" + nomeAtor + " trabalhou: \n");
        seriesEncontradas.forEach(s -> System.out.printf(s.getTitulo() + "- avaliacao" + s.getAvaliacao()));
    }

    private void buscarSeriePorAtorEAvaliacao(){
        System.out.printf("Qual o nome para busca?");
        var nomeAtor = leitura.nextLine();
        System.out.printf("Qual avaliacao (de 0 a 10) para busca?");
        var avaliacao = leitura.nextLine();
        List<Serie> seriesEncontradas = repository.findbyAtoresContainingIgnoreCaseAndAvaliacaoGreaterThanEqual(nomeAtor, avaliacao);
        System.out.printf("Series em que o ator" + nomeAtor + " trabalhou: \n");
        seriesEncontradas.forEach(s -> System.out.printf(s.getTitulo() + "- avaliacao" + s.getAvaliacao()));
    }

    private void buscarTop5Series(){
        List<Serie> serieTop = repository.findbyTop5OrderByAvaliacaoDesc();
        serieTop.forEach(serie -> System.out.printf(serie.getTitulo() + "- avaliacao" + serie.getAvaliacao()));
    }

    private void buscarSeriesPorCategoria(){
        System.out.printf("Qual categoria/genero deseja buscar?");
        var nomeGenero = leitura.nextLine();
        Categoria categoria = Categoria.fromPortugues(nomeGenero);
        List<Serie> seriesPorCategoria = repository.findbyGenero(categoria);
        System.out.printf("Series por categoria: " + nomeGenero);
        seriesPorCategoria.forEach(System.out::println);
    }

    private void filtrarSeriesPorTemporadaEAvaliacao(){
        System.out.println("Filtrar séries até quantas temporadas? ");
        var totalTemporadas = leitura.nextInt();
        leitura.nextLine();
        System.out.println("Com avaliação a partir de que valor? ");
        var avaliacao = leitura.nextDouble();
        leitura.nextLine();
        List<Serie> filtroSeries = repository.findByTotalTemporadasLessThanEqualAndAvaliacaoGreaterThanEqual(totalTemporadas, avaliacao);
        System.out.println("*** Séries filtradas ***");
        filtroSeries.forEach(s ->
                System.out.println(s.getTitulo() + "  - avaliação: " + s.getAvaliacao()));
    }

    private void buscarEpisodioPorTrecho(){
        System.out.println("Qual nome do episodio deseja consultar? ");
        var episodioTrecho = leitura.nextLine();
        List<Episodio> episodiosEncontrados = repository.episodiosPorTrecho(episodioTrecho);
        episodiosEncontrados.forEach(e ->
                System.out.printf("Serie: %s Temporada %s - Episodio %s - %s\n",
                        e.getSerie().getTitulo(), e.getTemporada(),
                        e.getNumeroEpisodio(), e.getTitulo()));
    }

    private void buscarTopEpisodioPorSerie(){
        buscarSeriePorTitulo();

        if(serieBusca.isPresent()){
            Serie serie = serieBusca.get();
            List<Episodio> topEpisodios = repository.topEpisodiosPorSerie(serie);
            topEpisodios.forEach(e ->
                    System.out.printf("Serie: %s Temporada %s - Episodio %s - %s Avaliacao %s\n",
                            e.getSerie().getTitulo(), e.getTemporada(),
                            e.getNumeroEpisodio(), e.getTitulo(), e.getAvaliacao()));
        }
    }

    private void buscarEpisodiosAPartirDeUmaData(){
        buscarSeriePorTitulo();
        if(serieBusca.isPresent()){
            Serie serie = serieBusca.get();
            System.out.printf("Digite o ano limite de lancamento: ");
            var anoLancamento = leitura.nextInt();
            leitura.nextLine();

            List<Episodio> episodioAno = repository.episodiosPorSerieEAno(serie, anoLancamento);
            episodioAno.forEach(System.out::println);
        }
    }
}