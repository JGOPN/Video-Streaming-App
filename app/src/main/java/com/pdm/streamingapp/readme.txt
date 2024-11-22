suponho que o proximo passo seja criar a screen que mostra a lista dos filmes.
criar um novo package sob /ui, e criar os novos ficheiros la (um ficheiro principal + um ficheiro viewModel)
para navegar para esta nova tela, ver /ui/navigation/StreamingAppNavGraph.

Para a lista dos filmes, sugiro usar ou uma LazyColumn ou LazyGrid, com um card para cada filme
(exemplos lazycolumn e card no CMSApp, MainScreen.kt)

Podes mostrar os filmes em data/Datasource.kt. feel free para alterar qqr coisa, inclusive os models
(por ex. se quiser adicionar alguma referencia a uma imagem para thumbnails)