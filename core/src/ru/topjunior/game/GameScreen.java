package ru.topjunior.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

import com.badlogic.gdx.math.Rectangle;
import java.util.Iterator;

public class GameScreen implements Screen {

	final Drop game;
	OrthographicCamera camera;
	SpriteBatch batch;  //предоставляет текстуру для рисования приямоугольника
	Texture img;		//декодирует файл изображения и загружает его в память графического процесса, находится в папке assets
	Texture dropImage;
	Texture bucketImage;
	Sound dropSound;
	Music rainMusic;
	Rectangle bucket;
	Vector3 touchPos;
	Array<Rectangle> raindrops;
	long lastDropTime;
	int dropsGatchered;



	public GameScreen (final Drop gam) {
		this.game = gam;

		// создается камера и SpriteBatch
		camera = new OrthographicCamera();
		camera.setToOrtho(false, 800, 480);
		batch = new SpriteBatch();

		touchPos = new Vector3();

		// загрузка изображений для капли и ведра, 64x64 пикселей каждый
		dropImage = new Texture("droplet.png");
		bucketImage = new Texture("bucket.png");

		// загрузка звукового эффекта падающей капли и фоновой "музыки" дождя
		dropSound = Gdx.audio.newSound(Gdx.files.internal("waterdrop.wav"));
		rainMusic = Gdx.audio.newMusic(Gdx.files.internal("undertreeinrain.mp3"));

		// сразу же воспроизводиться музыка для фона
		rainMusic.setLooping(true);
		rainMusic.play();

		// создается Rectangle для представления ведра
		bucket = new Rectangle();
		// центрируем ведро по горизонтали
		bucket.x = 800 / 2 - 64 / 2;   // ???
		// размещаем на 20 пикселей выше нижней границы экрана.
		bucket.y = 20;
		bucket.width = 64;
		bucket.height = 64;

		// создает массив капель и возрождает первую
		raindrops = new Array<Rectangle>();
		spawnRaindrop();

	}

	private void spawnRaindrop() {
		Rectangle raindrop = new Rectangle();
		raindrop.x = MathUtils.random(0, 800-64);
		raindrop.y = 480;
		raindrop.width = 64;
		raindrop.height = 64;
		raindrops.add(raindrop);
		lastDropTime = TimeUtils.nanoTime();
	}

	@Override
	//обновление логики игры, где сначало очистка экрана а потом рисование
		public void render (float delta) {
		// очищаем экран темно-синим цветом.
		// Аргументы для glClearColor красный, зеленый
		// синий и альфа компонент в диапазоне [0,1]
		// цвета используемого для очистки экрана.
		Gdx.gl.glClearColor(0, 0, 0.2f, 1);			// ???
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		// сообщает камере, что нужно обновить матрицы
		camera.update();

		// сообщаем SpriteBatch о системе координат
		// визуализации указанной для камеры.
		game.batch.setProjectionMatrix(camera.combined);

		// начинаем новую серию, рисуем ведро и
		// все капли
		game.batch.begin();
		game.font.draw(game.batch, "Raindrobs catched:" + dropsGatchered, 0 , 480);
		game.batch.draw(bucketImage, bucket.x, bucket.y);
		for (Rectangle raindrop: raindrops) {
			game.batch.draw(dropImage, raindrop.x, raindrop.y);
		}
		game.batch.end();

		// обработка пользовательского ввода
		if(Gdx.input.isTouched()){
			touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
			camera.unproject(touchPos);
			bucket.x = (int) (touchPos.x - 64 / 2);

		}

		if(Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.LEFT)) bucket.x -=200 * Gdx.graphics.getDeltaTime();
		if(Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.RIGHT)) bucket.x -=200 * Gdx.graphics.getDeltaTime();

		// убедитесь что ведро остается в пределах экрана
		if (bucket.x  < 0) bucket.x = 0;
		if (bucket.x > 800 - 64) bucket.x = 800 - 64;

		// проверка, нужно ли создавать новую каплю
		if(TimeUtils.nanoTime() - lastDropTime > 1000000000) spawnRaindrop();

		// движение капли, удаляем все капли выходящие за границы экрана
		// или те, что попали в ведро. Воспроизведение звукового эффекта
		// при попадании.
		Iterator<Rectangle> iter = raindrops.iterator();
		while (iter.hasNext()) {
			Rectangle raindrop = iter.next();
			raindrop.y -=200 * Gdx.graphics.getDeltaTime();
			if (raindrop.y + 64 < 0) iter.remove();
			if (raindrop.overlaps(bucket)) {
				dropsGatchered++;
				dropSound.play();
				iter.remove();
			}
		}
	}

	@Override
	public void resize(int width, int height) {

	}

	@Override
	public void pause() {

	}

	@Override
	public void resume() {

	}

	@Override
	public void hide() {

	}

	/*@Override
	public void resize(int width, int height) {  //вызывается при каждом изменение экрана в игре
		super.resize(width, height);
	}

	@Override
	public void pause() {		//вызывается когда нажата кнопка Home или при входящем звонке
		super.pause();
	}

	@Override
	public void resume() {
		super.resume();
	}
	*/

	@Override
	public void dispose() {   //вызывается когда приложение уничтожается
		// высвобождение всех нативных ресурсов
		dropImage.dispose();
		bucketImage.dispose();
		dropSound.dispose();
		rainMusic.dispose();
	}

	@Override
	public void show() {
		// воспроизведение фоновой музыки
		// когда отображается экрана
		rainMusic.play();
	}
}

// вся логика игры в этом классе